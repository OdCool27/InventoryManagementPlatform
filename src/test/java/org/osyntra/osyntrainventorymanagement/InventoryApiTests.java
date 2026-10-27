package org.osyntra.osyntrainventorymanagement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.osyntra.osyntrainventorymanagement.models.*;
import org.osyntra.osyntrainventorymanagement.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.flyway.default-schema=sentra_mvp_test",
        "spring.datasource.hikari.connection-init-sql=SET search_path TO sentra_mvp_test",
        "SENTRA_ADMIN_EMAIL=", "SENTRA_ADMIN_PASSWORD="
})
@AutoConfigureMockMvc
@Transactional
class InventoryApiTests {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JdbcClient jdbcClient;

    private static final String ADMIN = "admin-test@example.com";
    private static final String PASSWORD = "Test-only-password-123";

    @BeforeEach
    void createEmployee() {
        employeeRepository.save(new Employee("Test", "Admin", ADMIN, passwordEncoder.encode(PASSWORD),
                null, "Administrator", EmployeeRole.ADMIN));
    }

    @Test
    void productInventoryAndAuditWorkflow() throws Exception {
        String id = createProduct("BRAKE-1", "Toyota", "Corolla", "2018", null);
        mvc.perform(post("/api/inventory/" + id).with(user(ADMIN).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantityOnHand\":5,\"reorderLevel\":3}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.availableQuantity").value(5));
        mvc.perform(post("/api/inventory/" + id + "/add").with(user(ADMIN).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":4}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantityOnHand").value(9));
        mvc.perform(post("/api/inventory/" + id + "/remove").with(user(ADMIN).roles("MANAGER"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":6}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantityOnHand").value(3))
                .andExpect(jsonPath("$.lowStock").value(true));
        mvc.perform(put("/api/inventory/" + id + "/reorder-level").with(user(ADMIN).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"reorderLevel\":2}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.lowStock").value(false));
        mvc.perform(put("/api/inventory/" + id + "/quantity").with(user(ADMIN).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":0}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.availableQuantity").value(0));
        mvc.perform(get("/api/reports/summary").with(user(ADMIN).roles("MANAGER")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalProducts").value(1))
                .andExpect(jsonPath("$.totalUnits").value(0)).andExpect(jsonPath("$.outOfStockProducts").value(1));
        mvc.perform(get("/api/reports/low-stock").with(user(ADMIN).roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].product.itemId").value(id));
        mvc.perform(get("/api/reports/out-of-stock").with(user(ADMIN).roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/audit").param("entityId", id).with(user(ADMIN).roles("MANAGER")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0].action").value("QUANTITY_SET"))
                .andExpect(jsonPath("$[0].oldValue").value("3"))
                .andExpect(jsonPath("$[0].newValue").value("0"))
                .andExpect(jsonPath("$[0].userId").isNumber());
    }

    @Test
    void rejectsOverRemovalWithoutChangingQuantityOrAudit() throws Exception {
        String id = createStockedProduct(2, 1);
        mvc.perform(post("/api/inventory/" + id + "/remove").with(user(ADMIN).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":3}"))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/inventory/" + id).with(user(ADMIN).roles("STAFF")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantityOnHand").value(2));
        assertEquals(2L, jdbcClient.sql("SELECT count(*) FROM audit_logs WHERE entity_id = :id")
                .param("id", id).query(Long.class).single());
    }

    @Test
    void combinesCategoryVehicleAndTextFilters() throws Exception {
        String categoryBody = mvc.perform(post("/api/categories").with(user(ADMIN).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"BRAKING\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int categoryId = objectMapper.readTree(categoryBody).get("id").asInt();
        String match = createProduct("FILTER-MATCH", "Toyota", "Corolla", "2018", categoryId);
        createProduct("FILTER-OTHER", "Honda", "Civic", "2019", categoryId);
        mvc.perform(post("/api/inventory/" + match).with(user(ADMIN).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantityOnHand\":1,\"reorderLevel\":1}"))
                .andExpect(status().isCreated());
        for (String endpoint : new String[]{"/api/products", "/api/inventory"}) {
            mvc.perform(get(endpoint).with(user(ADMIN).roles("STAFF"))
                    .param("search", "FILTER-").param("category", "braking").param("make", "toyota")
                    .param("model", "Corolla").param("year", "2018"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        }
        mvc.perform(get("/api/products").with(user(ADMIN).roles("STAFF")).param("year", "2020"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/inventory").with(user(ADMIN).roles("STAFF")).param("lowStock", "true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"STAFF", "SUPERVISOR"})
    void readOnlyRolesCannotAdminister(String role) throws Exception {
        String id = createStockedProduct(5, 1);
        mvc.perform(get("/api/products").with(user(ADMIN).roles(role))).andExpect(status().isOk());
        mvc.perform(get("/api/inventory").with(user(ADMIN).roles(role))).andExpect(status().isOk());
        mvc.perform(post("/api/products").with(user(ADMIN).roles(role))
                .contentType(MediaType.APPLICATION_JSON).content(productJson("DENIED", null, null, null, null)))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/products/" + id).with(user(ADMIN).roles(role))
                .contentType(MediaType.APPLICATION_JSON).content(productJson("DENIED", null, null, null, null)))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/products/" + id).with(user(ADMIN).roles(role))).andExpect(status().isForbidden());
        mvc.perform(post("/api/inventory/" + id + "/add").with(user(ADMIN).roles(role))
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":1}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/inventory/" + id + "/remove").with(user(ADMIN).roles(role))
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":1}")).andExpect(status().isForbidden());
        mvc.perform(put("/api/inventory/" + id + "/quantity").with(user(ADMIN).roles(role))
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":1}")).andExpect(status().isForbidden());
        mvc.perform(put("/api/inventory/" + id + "/reorder-level").with(user(ADMIN).roles(role))
                .contentType(MediaType.APPLICATION_JSON).content("{\"reorderLevel\":1}")).andExpect(status().isForbidden());
        mvc.perform(get("/api/reports/summary").with(user(ADMIN).roles(role))).andExpect(status().isForbidden());
        mvc.perform(get("/api/audit").with(user(ADMIN).roles(role))).andExpect(status().isForbidden());
    }

    @Test
    void authenticatesActiveEmployeeAndRejectsBadCredentials() throws Exception {
        mvc.perform(get("/api/auth/me").with(httpBasic(ADMIN, PASSWORD))).andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(ADMIN)).andExpect(jsonPath("$.passwordHash").doesNotExist());
        mvc.perform(get("/api/auth/me").with(httpBasic(ADMIN, "wrong"))).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
        jdbcClient.sql("UPDATE users SET status = 'SUSPENDED' WHERE email = :email").param("email", ADMIN).update();
        mvc.perform(get("/api/auth/me").with(httpBasic(ADMIN, PASSWORD))).andExpect(status().isUnauthorized());
    }

    @Test
    void onlyAdminCanCreateEmployeeAndPasswordsAreHashed() throws Exception {
        String body = """
                {"firstName":"Sam","lastName":"Worker","email":"staff-test@example.com",
                 "password":"A-test-password-123","jobTitle":"Clerk","role":"STAFF"}
                """;
        mvc.perform(post("/api/employees").with(user(ADMIN).roles("MANAGER"))
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        mvc.perform(post("/api/employees").with(user(ADMIN).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("STAFF")).andExpect(jsonPath("$.passwordHash").doesNotExist());
        Employee employee = employeeRepository.findByEmail("staff-test@example.com").orElseThrow();
        assertTrue(passwordEncoder.matches("A-test-password-123", employee.getPasswordHash()));
        mvc.perform(post("/api/products").with(httpBasic(employee.getEmail(), "A-test-password-123"))
                .contentType(MediaType.APPLICATION_JSON).content(productJson("NO", null, null, null, null)))
                .andExpect(status().isForbidden());
    }

    @Test
    void validatesRequestsAndMissingResources() throws Exception {
        mvc.perform(post("/api/products").with(user(ADMIN).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/products").with(user(ADMIN).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content(productJson("BAD", "Toyota", null, null, null)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/products/not-a-uuid").with(user(ADMIN).roles("STAFF"))).andExpect(status().isBadRequest());
        mvc.perform(get("/api/inventory").param("limit", "0").with(user(ADMIN).roles("STAFF")))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/products/" + UUID.randomUUID()).with(user(ADMIN).roles("STAFF")))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/inventory/" + UUID.randomUUID()).with(user(ADMIN).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantityOnHand\":1,\"reorderLevel\":0}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/products").with(user(ADMIN).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content(productJson("BAD-CATEGORY", null, null, null, 2147483647)))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsNegativeInputsAndZeroAdjustments() throws Exception {
        String id = createStockedProduct(1, 1);
        for (String endpoint : new String[]{"/add", "/remove"}) {
            mvc.perform(post("/api/inventory/" + id + endpoint).with(user(ADMIN).roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":0}"))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(put("/api/inventory/" + id + "/quantity").with(user(ADMIN).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":-1}")).andExpect(status().isBadRequest());
        mvc.perform(put("/api/inventory/" + id + "/reorder-level").with(user(ADMIN).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"reorderLevel\":-1}")).andExpect(status().isBadRequest());
    }

    @Test
    void preventsDuplicateInventory() throws Exception {
        String id = createStockedProduct(1, 0);
        mvc.perform(post("/api/inventory/" + id).with(user(ADMIN).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantityOnHand\":1,\"reorderLevel\":0}"))
                .andExpect(status().isConflict());
    }

    @Test
    void preventsDuplicateSku() throws Exception {
        createProduct("UNIQUE", null, null, null, null);
        mvc.perform(post("/api/products").with(user(ADMIN).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content(productJson("UNIQUE", null, null, null, null)))
                .andExpect(status().isConflict());
    }

    @Test
    void managerCanEditAndDeactivateProducts() throws Exception {
        String id = createStockedProduct(5, 1);
        mvc.perform(put("/api/products/" + id).with(user(ADMIN).roles("MANAGER"))
                .contentType(MediaType.APPLICATION_JSON).content(productJson("EDITED", "Honda", "Civic", "2020", null)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.sku").value("EDITED"));
        mvc.perform(delete("/api/products/" + id).with(user(ADMIN).roles("MANAGER")))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/products").with(user(ADMIN).roles("STAFF")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/inventory").with(user(ADMIN).roles("STAFF")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/products/" + id).with(user(ADMIN).roles("STAFF")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));
        mvc.perform(post("/api/inventory/" + id + "/add").with(user(ADMIN).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":1}")).andExpect(status().isConflict());
    }

    @Test
    void databaseFunctionItselfPreventsNegativeStock() throws Exception {
        String id = createStockedProduct(1, 0);
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                () -> jdbcClient.sql("SELECT adjust_inventory(:id, -2, FALSE)")
                        .param("id", UUID.fromString(id)).query(Integer.class).single());
    }

    @Test
    void swaggerDescribesEndpointsAndBasicAuthentication() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/inventory/{productId}/add']").exists())
                .andExpect(jsonPath("$.components.schemas.StockRequest.properties.quantity.type").value("integer"))
                .andExpect(jsonPath("$.components.schemas.ProductRequest.properties.unitPrice.type").value("number"))
                .andExpect(jsonPath("$.components.securitySchemes.basicAuth.scheme").value("basic"));
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    private String createStockedProduct(int quantity, int reorder) throws Exception {
        String id = createProduct("STOCKED", null, null, null, null);
        mvc.perform(post("/api/inventory/" + id).with(user(ADMIN).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantityOnHand\":" + quantity + ",\"reorderLevel\":" + reorder + "}"))
                .andExpect(status().isCreated());
        return id;
    }

    private String createProduct(String sku, String make, String model, String year, Integer category) throws Exception {
        String response = mvc.perform(post("/api/products").with(user(ADMIN).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content(productJson(sku, make, model, year, category)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.get("itemId").asString();
    }

    private String productJson(String sku, String make, String model, String year, Integer category) {
        var request = new org.osyntra.osyntrainventorymanagement.dtos.product.ProductRequest(
                sku, null, "Brake Pad", "Front brake pad", new java.math.BigDecimal("25.50"), category, make, model, year);
        return objectMapper.writeValueAsString(request);
    }
}
