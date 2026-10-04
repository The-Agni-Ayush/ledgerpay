package io.github.theagniayush.ledgerpay.ledger;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration smoke test: needs PostgreSQL from docker-compose to be running
 * (run "docker compose up -d" first), otherwise use "-DskipTests".
 */
@SpringBootTest
class LedgerApplicationTests {

    @Test
    void contextLoads() {
    }

}
