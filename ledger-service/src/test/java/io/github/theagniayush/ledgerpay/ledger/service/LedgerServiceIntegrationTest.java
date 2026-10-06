package io.github.theagniayush.ledgerpay.ledger.service;

import io.github.theagniayush.ledgerpay.ledger.domain.Direction;
import io.github.theagniayush.ledgerpay.ledger.domain.LedgerAccount;
import io.github.theagniayush.ledgerpay.ledger.domain.LedgerEntry;
import io.github.theagniayush.ledgerpay.ledger.exception.IdempotencyConflictException;
import io.github.theagniayush.ledgerpay.ledger.exception.InsufficientFundsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests against the real PostgreSQL database.
 * Every test creates its own accounts, so tests do not interfere with each other.
 * Journal rows are append-only, so test data stays in the local database.
 */
@SpringBootTest
class LedgerServiceIntegrationTest {

    @Autowired
    private LedgerService ledger;

    private PostTransferCommand cmd(UUID from, UUID to, long amount) {
        return new PostTransferCommand(UUID.randomUUID(), from, to, amount, "INR");
    }

    private long balanceOf(LedgerAccount account) {
        return ledger.getAccount(account.getId()).getBalanceMinor();
    }

    /** Puts money into a new account by transferring it from a system account that may go negative. */
    private LedgerAccount fundedAccount(LedgerAccount system, long amount) {
        LedgerAccount account = ledger.createAccount("INR", false);
        ledger.postTransfer(cmd(system.getId(), account.getId(), amount));
        return account;
    }

    @Test
    void transferWritesBalancedEntriesAndMovesTheMoney() {
        LedgerAccount system = ledger.createAccount("INR", true);
        LedgerAccount alice = fundedAccount(system, 1_000);
        LedgerAccount bob = ledger.createAccount("INR", false);

        ledger.postTransfer(cmd(alice.getId(), bob.getId(), 400));

        assertThat(balanceOf(alice)).isEqualTo(600);
        assertThat(balanceOf(bob)).isEqualTo(400);

        List<LedgerEntry> bobEntries = ledger.getEntries(bob.getId(), 10);
        assertThat(bobEntries).hasSize(1);
        assertThat(bobEntries.getFirst().getDirection()).isEqualTo(Direction.CREDIT);
        assertThat(bobEntries.getFirst().getAmountMinor()).isEqualTo(400);

        // money is conserved: nothing was created or lost
        assertThat(balanceOf(system) + balanceOf(alice) + balanceOf(bob)).isZero();
    }

    @Test
    void repeatingTheSameTransferIdOnlyMovesMoneyOnce() {
        LedgerAccount system = ledger.createAccount("INR", true);
        LedgerAccount alice = fundedAccount(system, 1_000);
        LedgerAccount bob = ledger.createAccount("INR", false);
        PostTransferCommand command = cmd(alice.getId(), bob.getId(), 250);

        TransferResult first = ledger.postTransfer(command);
        TransferResult second = ledger.postTransfer(command);

        assertThat(first.replayed()).isFalse();
        assertThat(second.replayed()).isTrue();
        assertThat(balanceOf(alice)).isEqualTo(750);
        assertThat(balanceOf(bob)).isEqualTo(250);
        assertThat(ledger.getEntries(bob.getId(), 10)).hasSize(1);
    }

    @Test
    void sameTransferIdWithDifferentDetailsIsRejected() {
        LedgerAccount system = ledger.createAccount("INR", true);
        LedgerAccount alice = fundedAccount(system, 1_000);
        LedgerAccount bob = ledger.createAccount("INR", false);
        UUID transferId = UUID.randomUUID();

        ledger.postTransfer(new PostTransferCommand(transferId, alice.getId(), bob.getId(), 100, "INR"));

        assertThatThrownBy(() ->
                ledger.postTransfer(new PostTransferCommand(transferId, alice.getId(), bob.getId(), 999, "INR")))
                .isInstanceOf(IdempotencyConflictException.class);
        assertThat(balanceOf(alice)).isEqualTo(900);
    }

    @Test
    void transferBeyondTheBalanceIsRejectedAndChangesNothing() {
        LedgerAccount system = ledger.createAccount("INR", true);
        LedgerAccount alice = fundedAccount(system, 100);
        LedgerAccount bob = ledger.createAccount("INR", false);

        assertThatThrownBy(() -> ledger.postTransfer(cmd(alice.getId(), bob.getId(), 200)))
                .isInstanceOf(InsufficientFundsException.class);

        assertThat(balanceOf(alice)).isEqualTo(100);
        assertThat(balanceOf(bob)).isZero();
        assertThat(ledger.getEntries(bob.getId(), 10)).isEmpty();
    }

    @Test
    void parallelTransfersNeverOverdrawTheSourceAccount() throws Exception {
        LedgerAccount system = ledger.createAccount("INR", true);
        LedgerAccount source = fundedAccount(system, 1_000);
        LedgerAccount target = ledger.createAccount("INR", false);

        int threads = 20; // 20 x 100 requested, but only 1000 available
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger succeeded = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                ready.countDown();
                try {
                    go.await();
                    ledger.postTransfer(cmd(source.getId(), target.getId(), 100));
                    succeeded.incrementAndGet();
                } catch (Exception expected) {
                    // concurrent-update conflict or insufficient funds: both are acceptable outcomes
                }
                return null;
            }));
        }

        ready.await();   // wait until every thread is lined up
        go.countDown();  // release them all at the same moment
        for (Future<?> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }
        pool.shutdown();

        int ok = succeeded.get();
        assertThat(ok).isBetween(1, 10);
        assertThat(balanceOf(source)).isEqualTo(1_000 - 100L * ok).isGreaterThanOrEqualTo(0);
        assertThat(balanceOf(target)).isEqualTo(100L * ok);
    }
}