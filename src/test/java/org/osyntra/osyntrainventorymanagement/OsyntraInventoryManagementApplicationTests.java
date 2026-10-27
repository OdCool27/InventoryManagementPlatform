package org.osyntra.osyntrainventorymanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.flyway.default-schema=sentra_mvp_test",
        "spring.datasource.hikari.connection-init-sql=SET search_path TO sentra_mvp_test",
        "SENTRA_ADMIN_EMAIL=", "SENTRA_ADMIN_PASSWORD="
})
class OsyntraInventoryManagementApplicationTests {

    @Test
    void contextLoads() {
    }

}
