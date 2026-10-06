package io.github.theagniayush.ledgerpay.ledger.exception;

import java.util.UUID;

public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException(UUID transferId) {
        super("Transfer " + transferId + " already exists with different details");
    }
}
