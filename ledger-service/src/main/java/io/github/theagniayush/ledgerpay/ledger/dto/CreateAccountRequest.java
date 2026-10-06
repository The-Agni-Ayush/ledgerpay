package io.github.theagniayush.ledgerpay.ledger.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateAccountRequest(
        @NotNull
        @Pattern(regexp = "[A-Za-z]{3}", message = "must be a 3-letter currency code")
        String currency,
        boolean allowNegative
) {
}
