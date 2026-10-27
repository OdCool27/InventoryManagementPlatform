package org.osyntra.osyntrainventorymanagement;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LiveServerTests {
    private static final String SCHEMA = "sentra_smoke_" + UUID.randomUUID().toString().replace("-", "");
    private static final String EMAIL = "bootstrap-test@example.com";
    private static final String PASSWORD = UUID.randomUUID().toString();

    @Value("${local.server.port}")
    private int port;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.default-schema", () -> SCHEMA);
        registry.add("spring.datasource.hikari.connection-init-sql", () -> "SET search_path TO " + SCHEMA);
        registry.add("SENTRA_ADMIN_EMAIL", () -> EMAIL);
        registry.add("SENTRA_ADMIN_PASSWORD", () -> PASSWORD);
    }

    @Test
    void bootsFromFreshSchemaAndServesAuthenticatedApiAndSwagger() throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            String credentials = Base64.getEncoder().encodeToString((EMAIL + ":" + PASSWORD).getBytes(StandardCharsets.UTF_8));
            HttpResponse<String> me = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/auth/me"))
                    .header("Authorization", "Basic " + credentials).build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, me.statusCode());
            assertTrue(me.body().contains("\"role\":\"ADMIN\""));
            HttpResponse<String> docs = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v3/api-docs"))
                    .build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, docs.statusCode());
            assertTrue(docs.body().contains("/api/inventory/{productId}/add"));
            HttpResponse<String> ui = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/swagger-ui/index.html"))
                    .build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, ui.statusCode());
        }
    }

    @AfterAll
    static void removeOnlyGeneratedSchema(@org.springframework.beans.factory.annotation.Autowired JdbcClient jdbcClient) {
        if (!SCHEMA.matches("sentra_smoke_[a-f0-9]{32}")) {
            throw new IllegalStateException("Unexpected test schema");
        }
        jdbcClient.sql("DROP SCHEMA " + SCHEMA + " CASCADE").update();
    }
}
