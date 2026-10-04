package io.github.theagniayush.ledgerpay.payment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration smoke test: needs PostgreSQL from docker-compose to be running
 * (run "docker compose up -d" first), otherwise use "-DskipTests".
 */
@SpringBootTest
class PaymentApplicationTests {

    @Test
    void contextLoads() {
    }

}
