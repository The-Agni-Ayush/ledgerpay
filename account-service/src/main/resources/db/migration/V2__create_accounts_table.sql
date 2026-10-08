-- Remove the placeholder table created by V1
DROP TABLE schema_marker;

-- Customer-facing accounts. The id is the same as the matching ledger account id.
CREATE TABLE accounts (
                          id          UUID         PRIMARY KEY,
                          owner_name  VARCHAR(100) NOT NULL,
                          currency    VARCHAR(3)   NOT NULL,
                          status      VARCHAR(10)  NOT NULL CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED')),
                          version     BIGINT       NOT NULL DEFAULT 0,
                          created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
                          updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);