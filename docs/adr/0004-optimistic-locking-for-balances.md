# 4. Optimistic locking and database constraints protect balances

**Status:** Accepted

## Context
Two transfers can try to spend the same money at the same moment. Without protection both read the same balance, both see enough funds, and both succeed: a double spend. A ledger must make this impossible even under heavy parallel load.

## Decision
Defence in layers:
1. **Optimistic locking.** `LedgerAccount` carries a version number (JPA `@Version`). An update is applied only if the version is unchanged since the account was read; otherwise it fails with an optimistic-lock exception and the whole transfer rolls back. The API reports this as `409 Conflict` so the caller can retry.
2. **One database transaction per transfer.** The transfer row, both journal entries and both balance updates commit together or not at all.
3. **Database constraints as the last line of defence.** `CHECK (allow_negative OR balance_minor >= 0)` stops an overdraft even if application code has a bug, and triggers reject any `UPDATE` or `DELETE` on `transfers` and `ledger_entries`, so history is immutable.

Pessimistic row locks (`SELECT ... FOR UPDATE`) were considered and rejected for now: they make transfers wait on each other and need a consistent lock order to avoid deadlocks when two transfers touch the same accounts in opposite directions.

## Consequences
- No long-held row locks and no deadlocks between transfers.
- Under heavy contention on a single account some requests fail with `409` and must be retried by the caller. Automatic retry is planned for the resilience phase (Resilience4j).
- Correctness no longer depends on application code alone; the database refuses invalid states.
- A parallel integration test (many threads spending from one account) verifies that the balance never goes negative and that the books always balance.
