package io.github.theagniayush.ledgerpay.account.domain;

public enum AccountStatus {
    ACTIVE,
    FROZEN,
    CLOSED;

    /** Which status changes are allowed. CLOSED is final. */
    public boolean canMoveTo(AccountStatus target) {
        return switch (this) {
            case ACTIVE -> target == FROZEN || target == CLOSED;
            case FROZEN -> target == ACTIVE || target == CLOSED;
            case CLOSED -> false;
        };
    }
}