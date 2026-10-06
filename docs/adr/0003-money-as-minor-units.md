# 3. Money stored as integer minor units

**Status:** Accepted

## Context
Floating point numbers (`double`) cannot represent most decimal fractions exactly, so sums drift and money is silently lost or created. A ledger must add up exactly.

## Decision
All amounts are stored and processed as whole numbers of the smallest currency unit (paise for INR, cents for USD) in `BIGINT` columns and Java `long` fields, named `amount_minor` / `amountMinor` / `balance_minor`. Every amount also carries a 3-letter currency code, and a transfer is rejected if the currencies of the transfer and both accounts do not match. Conversion to a display format (for example 40000 paise to 400.00 rupees) happens only at the edge of the system, in the client.

## Consequences
- Arithmetic is exact: balances always add up and "total of all balances = 0" can be asserted in tests.
- Clients must send amounts in minor units (`amountMinor`), which has to be documented in the API.
- Currencies with a different number of decimal places (for example JPY has none) need a lookup when displaying amounts.
- `BIGINT` limits amounts to about 9.2 x 10^18 minor units, far beyond any realistic balance.
