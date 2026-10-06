package io.github.theagniayush.ledgerpay.ledger.service;

import io.github.theagniayush.ledgerpay.ledger.domain.LedgerAccount;
import io.github.theagniayush.ledgerpay.ledger.domain.LedgerEntry;

import java.util.List;
import java.util.UUID;

public interface LedgerService {

    /** Creates a new account with a zero balance. */
    LedgerAccount createAccount(String currency, boolean allowNegative);

    /** Returns the account or throws AccountNotFoundException. */
    LedgerAccount getAccount(UUID accountId);

    /** Returns the latest journal entries of an account, newest first (limit is capped at 100). */
    List<LedgerEntry> getEntries(UUID accountId, int limit);

    /**
     * Moves money between two accounts atomically. Repeating a request with the same
     * transferId and the same details is safe: it returns the original result.
     */
    TransferResult postTransfer(PostTransferCommand command);
}