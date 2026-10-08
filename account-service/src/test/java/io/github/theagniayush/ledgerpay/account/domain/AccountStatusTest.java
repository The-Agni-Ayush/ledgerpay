package io.github.theagniayush.ledgerpay.account.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class AccountStatusTest {

    @ParameterizedTest(name = "{0} -> {1} allowed = {2}")
    @CsvSource({
            "ACTIVE, FROZEN, true",
            "ACTIVE, CLOSED, true",
            "ACTIVE, ACTIVE, false",
            "FROZEN, ACTIVE, true",
            "FROZEN, CLOSED, true",
            "FROZEN, FROZEN, false",
            "CLOSED, ACTIVE, false",
            "CLOSED, FROZEN, false",
            "CLOSED, CLOSED, false"
    })
    void transitionRules(AccountStatus from, AccountStatus to, boolean allowed) {
        assertThat(from.canMoveTo(to)).isEqualTo(allowed);
    }
}