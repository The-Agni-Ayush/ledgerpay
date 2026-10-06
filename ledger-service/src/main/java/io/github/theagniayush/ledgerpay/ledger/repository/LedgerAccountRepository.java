package io.github.theagniayush.ledgerpay.ledger.repository;

import io.github.theagniayush.ledgerpay.ledger.domain.LedgerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerAccountRepository extends JpaRepository<LedgerAccount, UUID> {
}