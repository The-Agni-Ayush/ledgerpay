package io.github.theagniayush.ledgerpay.ledger.controller;

import io.github.theagniayush.ledgerpay.ledger.service.LedgerService;
import io.github.theagniayush.ledgerpay.ledger.service.PostTransferCommand;
import io.github.theagniayush.ledgerpay.ledger.service.TransferResult;
import io.github.theagniayush.ledgerpay.ledger.dto.AccountResponse;
import io.github.theagniayush.ledgerpay.ledger.dto.CreateAccountRequest;
import io.github.theagniayush.ledgerpay.ledger.dto.EntryResponse;
import io.github.theagniayush.ledgerpay.ledger.dto.TransferRequest;
import io.github.theagniayush.ledgerpay.ledger.dto.TransferResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @PostMapping("/accounts")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        var account = ledgerService.createAccount(request.currency(), request.allowNegative());
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.from(account));
    }

    @GetMapping("/accounts/{accountId}")
    public AccountResponse getAccount(@PathVariable UUID accountId) {
        return AccountResponse.from(ledgerService.getAccount(accountId));
    }

    @GetMapping("/accounts/{accountId}/entries")
    public List<EntryResponse> getEntries(@PathVariable UUID accountId,
                                          @RequestParam(defaultValue = "20") int limit) {
        return ledgerService.getEntries(accountId, limit).stream().map(EntryResponse::from).toList();
    }

    /** 201 when the transfer is new, 200 when the same transferId was already processed (safe retry). */
    @PostMapping("/transfers")
    public ResponseEntity<TransferResponse> postTransfer(@Valid @RequestBody TransferRequest request) {
        TransferResult result = ledgerService.postTransfer(new PostTransferCommand(
                request.transferId(), request.debitAccountId(), request.creditAccountId(),
                request.amountMinor(), request.currency()));
        HttpStatus status = result.replayed() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(TransferResponse.from(result.transfer(), result.replayed()));
    }
}