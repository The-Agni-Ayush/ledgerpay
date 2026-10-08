package io.github.theagniayush.ledgerpay.account.domain;

import io.github.theagniayush.ledgerpay.account.exception.InvalidAccountStatusException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    private Account newAccount() {
        return new Account(UUID.randomUUID(), "Alice", "INR");
    }

    @Test
    void newAccountStartsActive() {
        Account account = newAccount();

        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.isActive()).isTrue();
    }

    @Test
    void accountCanBeFrozenAndUnfrozen() {
        Account account = newAccount();

        account.changeStatus(AccountStatus.FROZEN);
        assertThat(account.getStatus()).isEqualTo(AccountStatus.FROZEN);
        assertThat(account.isActive()).isFalse();

        account.changeStatus(AccountStatus.ACTIVE);
        assertThat(account.isActive()).isTrue();
    }

    @Test
    void closedAccountCannotBeReopened() {
        Account account = newAccount();
        account.changeStatus(AccountStatus.CLOSED);

        assertThatThrownBy(() -> account.changeStatus(AccountStatus.ACTIVE))
                .isInstanceOf(InvalidAccountStatusException.class);

        assertThat(account.getStatus()).isEqualTo(AccountStatus.CLOSED); // unchanged after the failure
    }

    @Test
    void changingStatusUpdatesTheTimestamp() {
        Account account = newAccount();

        account.changeStatus(AccountStatus.FROZEN);

        assertThat(account.getUpdatedAt()).isAfterOrEqualTo(account.getCreatedAt());
    }
}