package io.github.theagniayush.ledgerpay.account.domain;

import io.github.theagniayush.ledgerpay.account.exception.InvalidAccountStatusException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // required by JPA
public class Account {

    /** Same value as the matching ledger account id. */
    @Id
    private UUID id;

    @Column(name = "owner_name", nullable = false, length = 100)
    private String ownerName;

    @Column(nullable = false, length = 3, updatable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AccountStatus status;

    /** Optimistic lock: two simultaneous changes to the same account cannot both succeed. */
    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Account(UUID id, String ownerName, String currency) {
        this.id = id;
        this.ownerName = ownerName;
        this.currency = currency;
        this.status = AccountStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /** Moves the account to a new status, if the rules allow it. */
    public void changeStatus(AccountStatus target) {
        if (!status.canMoveTo(target)) {
            throw new InvalidAccountStatusException(status, target);
        }
        this.status = target;
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }
}