package io.github.theagniayush.ledgerpay.ledger.service.impl;

import io.github.theagniayush.ledgerpay.ledger.domain.Direction;
import io.github.theagniayush.ledgerpay.ledger.domain.LedgerAccount;
import io.github.theagniayush.ledgerpay.ledger.domain.LedgerEntry;
import io.github.theagniayush.ledgerpay.ledger.domain.Transfer;
import io.github.theagniayush.ledgerpay.ledger.exception.AccountNotFoundException;
import io.github.theagniayush.ledgerpay.ledger.exception.IdempotencyConflictException;
import io.github.theagniayush.ledgerpay.ledger.exception.InvalidTransferException;
import io.github.theagniayush.ledgerpay.ledger.repository.LedgerAccountRepository;
import io.github.theagniayush.ledgerpay.ledger.repository.LedgerEntryRepository;
import io.github.theagniayush.ledgerpay.ledger.repository.TransferRepository;
import io.github.theagniayush.ledgerpay.ledger.service.LedgerService;
import io.github.theagniayush.ledgerpay.ledger.service.PostTransferCommand;
import io.github.theagniayush.ledgerpay.ledger.service.TransferResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerServiceImpl implements LedgerService {

    private final LedgerAccountRepository accounts;
    private final TransferRepository transfers;
    private final LedgerEntryRepository entries;

    @Override
    @Transactional
    public LedgerAccount createAccount(String currency, boolean allowNegative) {
        return accounts.save(new LedgerAccount(UUID.randomUUID(), currency.toUpperCase(), allowNegative));
    }

    @Override
    @Transactional(readOnly = true)
    public LedgerAccount getAccount(UUID accountId) {
        return accounts.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LedgerEntry> getEntries(UUID accountId, int limit) {
        getAccount(accountId); // throws 404 if the account does not exist
        int pageSize = Math.clamp(limit, 1, 100);
        return entries.findByAccountIdOrderByIdDesc(accountId, PageRequest.of(0, pageSize));
    }

    @Override
    @Transactional
    public TransferResult postTransfer(PostTransferCommand cmd) {
        String currency = cmd.currency().toUpperCase();

        // 1. Idempotency: has this transferId been processed already?
        Optional<Transfer> existing = transfers.findById(cmd.transferId());
        if (existing.isPresent()) {
            Transfer transfer = existing.get();
            if (!transfer.sameDetailsAs(cmd.debitAccountId(), cmd.creditAccountId(), cmd.amountMinor(), currency)) {
                throw new IdempotencyConflictException(cmd.transferId());
            }
            return new TransferResult(transfer, true);
        }

        // 2. Validate the request itself
        if (cmd.amountMinor() <= 0) {
            throw new InvalidTransferException("Amount must be greater than zero");
        }
        if (cmd.debitAccountId().equals(cmd.creditAccountId())) {
            throw new InvalidTransferException("Debit and credit accounts must be different");
        }

        // 3. Load both accounts and check the currencies
        LedgerAccount debitAccount = getAccount(cmd.debitAccountId());
        LedgerAccount creditAccount = getAccount(cmd.creditAccountId());
        if (!debitAccount.getCurrency().equals(currency) || !creditAccount.getCurrency().equals(currency)) {
            throw new InvalidTransferException("Currency mismatch between transfer and accounts");
        }

        // 4. Apply the balance changes (throws if the debit would overdraw the account)
        debitAccount.debit(cmd.amountMinor());
        creditAccount.credit(cmd.amountMinor());

        // 5. Save the transfer first and flush it, then write the two journal entries
        Transfer transfer = transfers.saveAndFlush(new Transfer(
                cmd.transferId(), cmd.debitAccountId(), cmd.creditAccountId(), cmd.amountMinor(), currency));
        entries.save(new LedgerEntry(transfer.getId(), debitAccount.getId(), Direction.DEBIT, cmd.amountMinor(), currency));
        entries.save(new LedgerEntry(transfer.getId(), creditAccount.getId(), Direction.CREDIT, cmd.amountMinor(), currency));

        return new TransferResult(transfer, false);
    }
}