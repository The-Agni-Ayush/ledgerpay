package io.github.theagniayush.ledgerpay.ledger.dto;

import io.github.theagniayush.ledgerpay.ledger.domain.Direction;
import io.github.theagniayush.ledgerpay.ledger.domain.LedgerEntry;

import java.time.Instant;
import java.util.UUID;

public record EntryResponse(
        Long id,
        UUID transferId,
        Direction direction,
        long amountMinor,
        String currency,
        Instant createdAt) {

    public static EntryResponse from(LedgerEntry entry) {
        return new EntryResponse(entry.getId(), entry.getTransferId(), entry.getDirection(),
                entry.getAmountMinor(), entry.getCurrency(), entry.getCreatedAt());
    }
}