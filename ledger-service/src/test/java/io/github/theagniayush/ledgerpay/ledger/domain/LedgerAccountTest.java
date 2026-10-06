package io.github.theagniayush.ledgerpay.ledger.domain;

import io.github.theagniayush.ledgerpay.ledger.exception.InsufficientFundsException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LedgerAccountTest {

    @Test
    void creditAndDebitChangeTheBalance() {
        LedgerAccount account = new LedgerAccount(UUID.randomUUID(), "INR", false);

        account.credit(1_000);
        account.debit(300);

        assertThat(account.getBalanceMinor()).isEqualTo(700);
    }

    @Test
    void normalAccountCannotBeOverdrawn() {
        LedgerAccount account = new LedgerAccount(UUID.randomUUID(), "INR", false);
        account.credit(100);

        assertThatThrownBy(() -> account.debit(101))
                .isInstanceOf(InsufficientFundsException.class);

        assertThat(account.getBalanceMinor()).isEqualTo(100); // unchanged after the failure
    }

    @Test
    void systemAccountMayGoNegative() {
        LedgerAccount system = new LedgerAccount(UUID.randomUUID(), "INR", true);

        system.debit(500);

        assertThat(system.getBalanceMinor()).isEqualTo(-500);
    }
}