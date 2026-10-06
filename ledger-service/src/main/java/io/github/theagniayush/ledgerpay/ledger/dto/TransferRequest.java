package io.github.theagniayush.ledgerpay.ledger.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record TransferRequest(
        @NotNull UUID transferId,
        @NotNull UUID debitAccountId,
        @NotNull UUID creditAccountId,
        @Positive long amountMinor,
        @NotNull
        @Pattern(regexp = "[A-Za-z]{3}", message = "must be a 3-letter currency code")
        String currency) {
}
