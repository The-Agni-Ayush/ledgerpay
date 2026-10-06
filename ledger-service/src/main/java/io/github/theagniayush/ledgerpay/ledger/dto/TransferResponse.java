package io.github.theagniayush.ledgerpay.ledger.dto;

import io.github.theagniayush.ledgerpay.ledger.domain.Transfer;

import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID transferId,
        UUID debitAccountId,
        UUID creditAccountId,
        long amountMinor,
        String currency,
        Instant createdAt,
        boolean replayed) {

    public static TransferResponse from(Transfer transfer, boolean replayed) {
        return new TransferResponse(transfer.getId(), transfer.getDebitAccountId(), transfer.getCreditAccountId(),
                transfer.getAmountMinor(), transfer.getCurrency(), transfer.getCreatedAt(), replayed);
    }
}