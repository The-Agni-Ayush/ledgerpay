-- Remove the placeholder table created by V1
DROP TABLE schema_marker;

-- Accounts known to the ledger. balance_minor is in minor units (paise/cents).
CREATE TABLE ledger_accounts (
                                 id             UUID PRIMARY KEY,
                                 currency       VARCHAR(3)  NOT NULL,
                                 balance_minor  BIGINT      NOT NULL DEFAULT 0,
                                 allow_negative BOOLEAN     NOT NULL DEFAULT FALSE,
                                 version        BIGINT      NOT NULL DEFAULT 0,
                                 created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                                 CONSTRAINT chk_balance_non_negative CHECK (allow_negative OR balance_minor >= 0)
);

-- One row per money movement; the id is chosen by the caller (idempotency key).
CREATE TABLE transfers (
                           id                UUID PRIMARY KEY,
                           debit_account_id  UUID        NOT NULL REFERENCES ledger_accounts (id),
                           credit_account_id UUID        NOT NULL REFERENCES ledger_accounts (id),
                           amount_minor      BIGINT      NOT NULL CHECK (amount_minor > 0),
                           currency          VARCHAR(3)  NOT NULL,
                           created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                           CONSTRAINT chk_different_accounts CHECK (debit_account_id <> credit_account_id)
);

-- Append-only journal: two rows per transfer (one DEBIT, one CREDIT).
CREATE TABLE ledger_entries (
                                id           BIGSERIAL PRIMARY KEY,
                                transfer_id  UUID        NOT NULL REFERENCES transfers (id),
                                account_id   UUID        NOT NULL REFERENCES ledger_accounts (id),
                                direction    VARCHAR(6)  NOT NULL CHECK (direction IN ('DEBIT', 'CREDIT')),
                                amount_minor BIGINT      NOT NULL CHECK (amount_minor > 0),
                                currency     VARCHAR(3)  NOT NULL,
                                created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_entries_account  ON ledger_entries (account_id, id DESC);
CREATE INDEX idx_entries_transfer ON ledger_entries (transfer_id);

-- Immutability: transfers and entries can never be updated or deleted.
CREATE FUNCTION forbid_modification() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION '% is append-only: % is not allowed', TG_TABLE_NAME, TG_OP;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_entries_append_only
    BEFORE UPDATE OR DELETE ON ledger_entries
    FOR EACH ROW EXECUTE FUNCTION forbid_modification();

CREATE TRIGGER trg_transfers_append_only
    BEFORE UPDATE OR DELETE ON transfers
    FOR EACH ROW EXECUTE FUNCTION forbid_modification();