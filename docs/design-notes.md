# Design notes

## Report features carried into this version

The source report describes admin login, registering and approving users, category setup, a news quantization date, article create/update/delete, keyword search, transaction history, ledger browsing, and category/popularity charts. Those are represented by the app's user, news, settings, search, ledger, and analytics flows.

The report also discusses permissionless ledger participants, consensus, CP-ABE, DDoS resilience, and reputation incentives. These are not claimed as implemented. This portfolio version is a centralized Java/MySQL demonstration with an append-only application ledger and manual review.

## Hash formats

Article fingerprints hash the UTF-8 text `categoryId|trimmedTitle|trimmedDescription|trimmedPublisher|ISO-date`. A block hash hashes `index|articleId|version|ISO-instant|contentHash|previousHash`. Pipes delimit fields and are excluded from normal form validation by convention; this is an educational, deterministic format, not a protocol for cross-organization consensus.

## Deletion

Article deletion stores the previous article version, marks the current row deleted, and appends a `DELETED` fingerprint block. Ledger rows remain attached to the article, preserving history while the current article is hidden from search.
