package org.osyntra.osyntrainventorymanagement;

import org.junit.jupiter.api.Test;
import org.osyntra.osyntrainventorymanagement.dtos.inventory.InventoryRequest;
import org.osyntra.osyntrainventorymanagement.dtos.product.ProductRequest;
import org.osyntra.osyntrainventorymanagement.models.*;
import org.osyntra.osyntrainventorymanagement.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(properties = {
        "spring.flyway.default-schema=sentra_mvp_test",
        "spring.datasource.hikari.connection-init-sql=SET search_path TO sentra_mvp_test",
        "SENTRA_ADMIN_EMAIL=", "SENTRA_ADMIN_PASSWORD="
})
@AutoConfigureMockMvc
class InventoryConcurrencyTests {
    @Autowired private MockMvc mvc;
    @Autowired private JdbcClient jdbcClient;
    @Autowired private ProductRepository productRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private PlatformTransactionManager transactionManager;

    @Test
    void concurrentRemovalsDoNotOversellAndOnlySuccessfulChangesAreAudited() throws Exception {
        String email = "concurrency-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        Employee employee = new Employee("Concurrent", "Test", email, "unused-test-hash", null, "Test", EmployeeRole.ADMIN);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        UUID id = transaction.execute(status -> {
            employeeRepository.save(employee);
            UUID productId = productRepository.create(new ProductRequest("CONCURRENT-" + UUID.randomUUID(), null,
                    "Concurrency Test", "Temporary integration test product", BigDecimal.ONE, null, null, null, null));
            inventoryRepository.create(productId, new InventoryRequest(5, 0));
            return productId;
        });
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            CountDownLatch start = new CountDownLatch(1);
            Callable<Integer> remove = () -> {
                assertTrue(start.await(10, TimeUnit.SECONDS));
                return mvc.perform(post("/api/inventory/" + id + "/remove").with(user(email).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":4}"))
                        .andReturn().getResponse().getStatus();
            };
            Future<Integer> first = executor.submit(remove);
            Future<Integer> second = executor.submit(remove);
            start.countDown();
            int firstStatus = first.get(15, TimeUnit.SECONDS);
            int secondStatus = second.get(15, TimeUnit.SECONDS);
            assertTrue((firstStatus == 200 && secondStatus == 409) || (firstStatus == 409 && secondStatus == 200));
            assertEquals(1, inventoryRepository.findByProductId(id, false).orElseThrow().getQuantityOnHand());
            assertEquals(1L, jdbcClient.sql("SELECT count(*) FROM audit_logs WHERE entity_id = :id")
                    .param("id", id.toString()).query(Long.class).single());
        } finally {
            transaction.executeWithoutResult(status -> {
                jdbcClient.sql("DELETE FROM audit_logs WHERE entity_id = :id").param("id", id.toString()).update();
                jdbcClient.sql("DELETE FROM inventory WHERE product_id = :id").param("id", id).update();
                jdbcClient.sql("DELETE FROM products WHERE id = :id").param("id", id).update();
                jdbcClient.sql("DELETE FROM employees WHERE user_id = :id").param("id", employee.getId()).update();
                jdbcClient.sql("DELETE FROM users WHERE id = :id").param("id", employee.getId()).update();
            });
        }
    }
}
