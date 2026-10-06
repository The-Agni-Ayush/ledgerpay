package io.github.theagniayush.ledgerpay.ledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transfer {

    /** Supplied by the caller; acts as the idempotency key. */
    @Id
    private UUID id;

    @Column(name = "debit_account_id", nullable = false, updatable = false)
    private UUID debitAccountId;

    @Column(name = "credit_account_id", nullable = false, updatable = false)
    private UUID creditAccountId;

    @Column(name = "amount_minor", nullable = false, updatable = false)
    private long amountMinor;

    @Column(nullable = false, length = 3, updatable = false)
    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Transfer(UUID id, UUID debitAccountId, UUID creditAccountId, long amountMinor, String currency) {
        this.id = id;
        this.debitAccountId = debitAccountId;
        this.creditAccountId = creditAccountId;
        this.amountMinor = amountMinor;
        this.currency = currency;
        this.createdAt = Instant.now();
    }

    /** True if a repeated request carries exactly the same details as this stored transfer. */
    public boolean sameDetailsAs(UUID debitAccountId, UUID creditAccountId, long amountMinor, String currency) {
        return this.debitAccountId.equals(debitAccountId)
                && this.creditAccountId.equals(creditAccountId)
                && this.amountMinor == amountMinor
                && this.currency.equals(currency);
    }
}