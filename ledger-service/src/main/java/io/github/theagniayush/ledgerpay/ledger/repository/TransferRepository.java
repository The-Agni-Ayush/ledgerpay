package io.github.theagniayush.ledgerpay.ledger.repository;

import io.github.theagniayush.ledgerpay.ledger.domain.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {
}