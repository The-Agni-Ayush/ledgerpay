package io.github.theagniayush.ledgerpay.ledger.domain;

import io.github.theagniayush.ledgerpay.ledger.exception.InsufficientFundsException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // required by JPA, hidden from your own code
public class LedgerAccount {

    @Id
    private UUID id;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "balance_minor", nullable = false)
    private long balanceMinor;

    @Column(name = "allow_negative", nullable = false)
    private boolean allowNegative;

    /** Optimistic lock: two concurrent updates to the same account cannot both succeed. */
    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public LedgerAccount(UUID id, String currency, boolean allowNegative) {
        this.id = id;
        this.currency = currency;
        this.allowNegative = allowNegative;
        this.balanceMinor = 0;
        this.createdAt = Instant.now();
    }

    /** Money leaves this account. */
    public void debit(long amountMinor) {
        if (!allowNegative && balanceMinor < amountMinor) {
            throw new InsufficientFundsException(id, balanceMinor, amountMinor);
        }
        balanceMinor -= amountMinor;
    }

    /** Money arrives in this account. */
    public void credit(long amountMinor) {
        balanceMinor += amountMinor;
    }
}