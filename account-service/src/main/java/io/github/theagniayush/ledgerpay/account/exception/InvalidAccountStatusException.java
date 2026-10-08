package io.github.theagniayush.ledgerpay.account.exception;

import io.github.theagniayush.ledgerpay.account.domain.AccountStatus;

public class InvalidAccountStatusException extends RuntimeException {

    public InvalidAccountStatusException(AccountStatus from, AccountStatus to) {
        super("Cannot change account status from " + from + " to " + to);
    }
}
