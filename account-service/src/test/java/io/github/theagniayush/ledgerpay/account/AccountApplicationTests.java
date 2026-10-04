package io.github.theagniayush.ledgerpay.account;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration smoke test: needs PostgreSQL from docker-compose to be running
 * (run "docker compose up -d" first), otherwise use "-DskipTests".
 */
@SpringBootTest
class AccountApplicationTests {

    @Test
    void contextLoads() {
    }

}
