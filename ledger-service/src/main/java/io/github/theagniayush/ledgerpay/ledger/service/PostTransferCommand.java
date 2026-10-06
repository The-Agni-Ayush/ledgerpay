package io.github.theagniayush.ledgerpay.ledger.service;

import java.util.UUID;

public record PostTransferCommand(
        UUID transferId,
        UUID debitAccountId,
        UUID creditAccountId,
        long amountMinor,
        String currency) {
}