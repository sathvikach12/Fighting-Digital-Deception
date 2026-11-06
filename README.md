# Fighting Digital Deception

Fighting Digital Deception is a Java 17 MVC web application for publishing and searching sample news, moderator review, approval-based accounts, and tamper-evident content history. It adapts the report's administration, user authorization, category management, quantization date, keyword filtering, keyword search history, and news ranking features to a small local portfolio application.

> **Integrity model:** this is a centralized hash-chain demonstration stored in MySQL. It is not a decentralized blockchain, does not use consensus or a peer-to-peer network, and cannot establish whether a story is true. SHA-256 confirms that content matches a recorded fingerprint; it does not prove the source or factual accuracy. Keyword rules only request human review.

## Features

- Admin and user sign-in with BCrypt password hashes and session-based role checks.
- Public registration followed by administrator approval; accounts can be activated or deactivated. Users can change their password from Profile.
- News article create, edit, view, search, and soft delete. Every edit snapshots the previous version and adds a new ledger block.
- Category administration and a configurable publication-date cutoff for current/trending grouping.
- Configurable suspicious phrases, matched-term display, a moderation queue, and recorded human review decisions.
- Search by title/description and category, with per-account and administrator search history.
- Genesis block, content fingerprints, previous-hash links, chain recomputation, and latest-article fingerprint check.
- Category totals and popular stories visualized with Chart.js.
- Prepared statements, try-with-resources, form validation, output escaping via JSTL, CSRF checks, and environment-configured JDBC.

## Stack

Java 17, Jakarta Servlets 6 / JSP 3.1, JDBC, MySQL 8, Maven, Tomcat 10.1, Bootstrap 5, Chart.js, BCrypt, SHA-256, JUnit 5.

## Architecture

`JSP view → Servlet controller → Service → DAO → MySQL`

JSP files live under `WEB-INF/views` so a client cannot request them directly. Servlet mappings are annotation-based. `AuthFilter` enforces session status and administrator access. The ledger append runs in the same database transaction as an article create/edit/delete. Before an edit is applied, the existing version is copied to `news_versions`; the new version receives a fresh content fingerprint and block. Database triggers reject ledger row updates and deletes.

## Requirements

- A 64-bit JDK 17 (set `JAVA_HOME` to its installation folder).
- Maven 3.9 or newer.
- MySQL Server 8.0.
- Apache Tomcat 10.1.

The project was compiled and its unit tests passed with JDK 17. In this environment, Maven's WAR packaging plugin could not be retrieved because Maven Central was unreachable, so a standard WAR layout was assembled from the compiled classes and cached dependencies for local Tomcat deployment. The MySQL-backed workflows still need a configured MySQL 8 database for full functional verification.

## Windows setup

Open PowerShell in the project directory. Create the schema:

```powershell
mysql -u root -p < database/schema.sql
```

Generate BCrypt hashes for the two demo passwords using the same BCrypt library as the app. From PowerShell, run the included helper after packaging once:

```powershell
./scripts/seed-demo.ps1
```

The helper generates fresh cost-12 BCrypt hashes, substitutes them into a temporary seed file, and runs MySQL. It prompts for the MySQL password. The demo credentials are the passwords shown below.

Set the database variables for the current PowerShell session (the application uses an empty local root password only when `DB_PASSWORD` is unset):

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/truthchain?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
$env:DB_USER = 'root'
$env:DB_PASSWORD = 'your-local-mysql-password'
```

Build and test:

```powershell
mvn test
mvn clean package
```

Copy `target\truthchain-news.war` to `%CATALINA_HOME%\webapps\truthchain-news.war`, start Tomcat with `%CATALINA_HOME%\bin\startup.bat`, then browse to `http://localhost:8080/truthchain-news/`.

## Demo accounts

| Role | Email | Password | State |
| --- | --- | --- | --- |
| Admin | `admin@truthchain.local` | `AdminDemo!2026` | Active |
| Approved user | `reader@truthchain.local` | `ReaderDemo!2026` | Active |
| Pending user | `pending@truthchain.local` | `ReaderDemo!2026` | Awaiting approval |

These are fictional demo credentials. Replace seeded accounts before exposing an instance to a network. The seed contains BCrypt hashes, never plaintext passwords.

## Database

`database/schema.sql` creates users, categories, news, prior news versions, suspicious keywords, moderation records and matches, append-only ledger blocks, search history, and app settings. `database/seed.sql` adds demo users, categories, keywords, and a date setting. Seeded example articles are intentionally entered from the admin UI so their content hashes and ledger entries are generated by the same application code as normal records.

## Screenshots

Add screenshots of the landing page, admin dashboard, moderation queue, and ledger explorer here after running the app locally.

## Tests

Run `mvn test`. Unit tests cover SHA-256 digest and block hashing, canonical article fingerprints, suspicious keyword matching, quantization cutoff behavior, and common input validation. Database integration tests are not included; the web flow requires MySQL and Tomcat for end-to-end checks.

## Known limitations

- The application is centralized. A database administrator who can alter both article records and the ledger can rewrite history; production-grade anchoring or independently held signatures are not implemented.
- Fingerprints establish integrity against the stored hash, not authorship, provenance, or factual truth.
- Keyword matching is literal substring matching and needs a human reviewer; it is not a classifier or fact-checking engine.
- CP-ABE, decentralized consensus, peer-to-peer distribution, DDoS protection, and fact-checker incentives discussed as concepts in the report are not implemented.
- Search uses SQL `LIKE`; search history is retained for auditability, and no user-facing history deletion flow is included.
- External Bootstrap and Chart.js assets are loaded from jsDelivr, so those visual enhancements need internet access in a browser.
- Set secure cookies and HTTPS at the Tomcat/reverse-proxy layer before deploying outside localhost.

## Future improvements

- Add integration tests against a disposable MySQL instance and automated Tomcat deployment checks.
- Anchor signed ledger checkpoints outside the app database.
- Add source/provenance fields, independent fact-check references, and review dispute history.
- Add pagination and a carefully designed search-history retention policy.
- Bundle front-end assets locally for offline operation.

## Repository hygiene

The `.gitignore` excludes Maven output, IDE state, logs, and environment files. No credentials or `.env` file belong in this repository.
