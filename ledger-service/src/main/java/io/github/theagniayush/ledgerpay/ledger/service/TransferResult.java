package io.github.theagniayush.ledgerpay.ledger.service;

import io.github.theagniayush.ledgerpay.ledger.domain.Transfer;

/** replayed = true when the same transferId had already been processed. */
public record TransferResult(Transfer transfer, boolean replayed) {
}