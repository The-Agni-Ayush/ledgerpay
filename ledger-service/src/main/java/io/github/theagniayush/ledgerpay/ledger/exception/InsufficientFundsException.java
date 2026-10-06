package io.github.theagniayush.ledgerpay.ledger.exception;

import java.util.UUID;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(UUID accountId, long balance, long requested) {
        super("Insufficient funds in account " + accountId + " : balance " + balance + " , requested " + requested);
    }
}
