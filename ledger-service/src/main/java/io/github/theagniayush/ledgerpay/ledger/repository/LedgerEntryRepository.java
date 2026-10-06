package io.github.theagniayush.ledgerpay.ledger.repository;

import io.github.theagniayush.ledgerpay.ledger.domain.LedgerEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByAccountIdOrderByIdDesc(UUID accountId, Pageable pageable);
}