package io.github.theagniayush.ledgerpay.ledger.dto;

import io.github.theagniayush.ledgerpay.ledger.domain.LedgerAccount;

import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String currency,
        long balanceMinor,
        boolean allowNegative,
        Instant createdAt) {

    public static AccountResponse from(LedgerAccount account) {
        return new AccountResponse(account.getId(), account.getCurrency(), account.getBalanceMinor(),
                account.isAllowNegative(), account.getCreatedAt());
    }
}