# PriceTracker

## Team
Bohdan Kocevyč, Dmitrii Khokhlov, Kyryl Bondarenko, Aleksandr Akimov

## Links
Shared document https://shorturl.at/f2NEd

## Project description
PriceTracker is an analytics and notification tool that tracks the prices of
computer components in popular online stores (e.g. Alza, CZC) and lets users
analyze price trends over time. It is not an e-commerce platform — it has no
shopping cart or payments, it only collects, stores, and displays prices, and
notifies users when the price of a tracked product drops below a set
threshold.

The system is built as **microservices** (Java 17 + Spring Boot) that
communicate with each other asynchronously via **Apache Kafka**, store data in
**PostgreSQL**, and expose a single unified gateway via **BFF (Backend for
Frontend)**.

---

## 1. Microservice architecture

| Service | Port | Responsibility |
|---|---|---|
| `user-service` | 8081 | Registration, login, JWT issuing, roles (USER/ADMIN) |
| `product-catalog-service` | 8082 | Catalog, price history, watchlist, comments, alerts, CSV export. Consumes `price-updates`, produces `price-drops` |
| `scraper-service` | 8083 | Strategy pattern (Alza/CZC), `@Scheduled` scraping, produces `price-updates`, admin endpoints for shops + dashboard |
| `notification-service` | 8084 | Consumes `price-drops`, sends emails (Observer), stores `notification_logs` |
| `bff` | 8085 | Backend for Frontend — aggregates data from multiple services, unified API for the frontend |

Infrastructure (Docker):

| Component | Port | Note |
|---|---|---|
| PostgreSQL | 5432 | Mapped to 5432 on the host (container internally on 5432) to avoid colliding with a locally installed PostgreSQL |
| pgAdmin | 5050 | Web interface for the database |
| Kafka | 29092 | Broker (host listener) |
| Zookeeper | 2181 | Kafka coordination |
| Elasticsearch | 9200 | Full-text search for the product catalog |
| Kibana | 5601 | Web UI for Elasticsearch |
| Mailhog | 1025 / 8025 | Development SMTP (1025) + web mailbox (8025) |

### Design patterns in the code
* **Strategy** — `ScraperStrategy` + `AlzaScraperStrategy`, `CzcScraperStrategy`, `ScraperStrategyFactory` (adding a new e-shop without touching the core, NFRQ4)
* **Observer** — `PriceDropProducer` (catalog) emits an event that notification-service processes via Kafka
* **BFF** — the `bff` module with clients for each downstream service
* **Builder** — `ProductDetailResponse.builder()`, `AggregatedProductDetail.builder()`
* **Singleton** — Spring beans (a single DB/Kafka connection instance per service)
* **Load Balancer** — nginx edge-proxy ([`nginx/default.conf`](nginx/default.conf)) in front of the BFF

---

## What is done, and where the functionality lives

### Functional requirements (FRQ)

| Requirement | Where it is |
|---|---|
| FRQ1 — search by name/category | `product-catalog` → `ProductController.getProducts` → `search/ProductSearchService` (Elasticsearch) with fallback to `ProductRepository.search` (DB) |
| FRQ2 — price history chart | `ProductController.getProduct` → `ProductService.getProductDetail` (`priceHistory` field) |
| FRQ3 — registration and login | `user-service` → `controller/AuthController`, `service/UserService`, JWT in `common-shared` `security/JwtService` |
| FRQ4 — adding to a watchlist | `product-catalog` → `controller/WatchlistController`, `service/WatchlistService` |
| FRQ5 — periodic scraping (1×/day) | `scraper-service` → `service/ScrapingOrchestrator` (`@Scheduled`) |
| FRQ6 — pagination and filtering | `ProductController.getProducts` (`page`, `size`, `minPrice`, `maxPrice`, `shopName`) |
| FRQ7 — scraper admin dashboard | `scraper-service` → `controller/ScraperAdminController` + dashboard `service`; via BFF `controller/BffAdminController` |
| FRQ8 — export history to CSV | `product-catalog` → `service/PriceHistoryExportService`, endpoint `/api/products/{id}/export` |

### Use cases (UC1–UC8)

| UC | Where it is |
|---|---|
| UC1 product search | `ProductController.getProducts` |
| UC2 view price history | `ProductService.getProductDetail` |
| UC3 add to watchlist | `WatchlistController` / `WatchlistService` |
| UC4 process new prices | `kafka/PriceUpdateConsumer` → `AlertEvaluationService` → `kafka/PriceDropProducer` |
| UC5 comment / review | `CommentController` / `CommentService` |
| UC6 export price history | `PriceHistoryExportService` |
| UC7 check scraper status | `ScraperLog` + dashboard endpoint |
| UC8 configure a new shop | admin `POST /api/admin/shops` → `ShopService` (+ `ScraperStrategyFactory`) |

### Non-functional requirements (NFRQ)

| Requirement | Where it is |
|---|---|
| NFRQ1 — microservices | 5 separate services + `docker-compose.yml` |
| NFRQ3 — hashed passwords, secured API | `user-service` BCrypt (`UserService`), JWT, service-to-service `security/InternalTokenValidator` + `InternalIdentityFilter` |
| NFRQ4 — easy to add a new e-shop (Strategy) | `scraper-service` `strategy/ScraperStrategy` + `…Factory` |
| NFRQ5 — resilience against an e-shop outage | `strategy/AlzaScraperStrategy` (try/catch, no crash), per-shop isolation in `ScrapingOrchestrator` |
| NFRQ6 — data retention (3 years) | `product-catalog` `application.properties` `catalog.price-retention-*` (scheduled cleanup) |

### Optional / extra-credit parts

| Part | Where it is |
|---|---|
| **Elasticsearch** (full-text search) | `product-catalog` → `search/ProductSearchService`, `search/ProductSearchDocument`; dependency `spring-boot-starter-data-elasticsearch` |
| **Cache (Hazelcast)** | `product-catalog` → `config/CacheConfig` (`@EnableCaching` + embedded Hazelcast); `@Cacheable` on `ProductService.getProductDetail`, invalidation via `@CacheEvict` on a new price in `kafka/PriceUpdateConsumer` |
| **Interceptor** (request logging) | `bff` → `interceptor/RequestLoggingInterceptor` + registration in `config/WebConfig` |
| **Frontend** (test UI) | [`frontend/index.html`](frontend/index.html) |
| **Real scraping via a headless browser** | `scraper-service` → `browser/PlaywrightPriceFetcher` (Playwright/Chromium), jsoup fallback in `AlzaScraperStrategy` |
| **Deployment to a production server** | `*/Dockerfile` + [`docker-compose.prod.yml`](docker-compose.prod.yml) (the whole stack with one command); guide [`docs/DEPLOYMENT.md`](docs/DEPLOYMENT.md) |

---

## Initialization steps and seed data

**Deploy (locally):** see [§3 Running the project](#3-running-the-project). In short: `docker compose up -d` (infrastructure) → `mvn -DskipTests install` → start the 5 services.

**Database:** on first start, the PostgreSQL container runs [`docker/postgres-init.sql`](docker/postgres-init.sql), which creates **a database for each service** (`pricetracker_user_db`, `…_catalog_db`, `…_scraper_db`, `…_notification_db`). Tables are created by Hibernate (`ddl-auto=update`) when each service starts.

**Seeded data (created automatically on startup):**

| What | Created in | Values |
|---|---|---|
| **Admin account** | `user-service` → `config/AdminBootstrapSeeder` | email `admin@pricetracker.local`, login `admin`, password `admin123`, role `ADMIN` (can be changed in `application.properties` `app.admin.*`) |
| **Default shops** | `scraper-service` → `config/DefaultShopSeeder` | `Alza`, `CZC` (both `active`) |

> **Products are not seeded** — they are created by the admin (frontend *Admin · Create product*, or `POST /api/admin/products`). The first scrape then fills in the price history.

---

## 2. Prerequisites

- **JDK 17+**
- **Maven 3.9+**
- **Docker Desktop** (PostgreSQL, Kafka, Mailhog)
- A REST client for POST requests — the IntelliJ HTTP Client or the *REST Client*
  extension in VS Code can run the included [`api.http`](api.http) file.

---

## 3. Running the project — two scenarios

The project can be run in two ways:

- **Scenario A — local development** ([`docker-compose.yml`](docker-compose.yml)): only the infrastructure runs in Docker, the 5 services run on the host via Maven. Lightweight, fast iteration, IDE debugging.
- **Scenario B — full stack in Docker** ([`docker-compose.prod.yml`](docker-compose.prod.yml)): everything (infra + 5 services + nginx with the frontend) with one command. Suited for deployment/demo.

### Scenario A — local development

**1. Infrastructure** (PostgreSQL, Kafka, Elasticsearch, Mailhog, …):
```bash
docker compose up -d
docker compose ps
```

**2. Build:**
```bash
mvn -DskipTests install
```

**3. Start the 5 services** (each in a separate terminal):
```bash
mvn -pl user-service spring-boot:run             # 8081
mvn -pl product-catalog-service spring-boot:run  # 8082
mvn -pl scraper-service spring-boot:run          # 8083
mvn -pl notification-service spring-boot:run     # 8084
mvn -pl bff spring-boot:run                      # 8085
```
Order doesn't matter; a service is ready once you see the log `Started ...Application`.

**4. Frontend** (a static server on localhost, due to CORS):
```bash
python -m http.server 5173 --directory frontend
```
App: <http://localhost:5173> · API directly on the BFF: <http://localhost:8085>.

### Scenario B — full stack in Docker

Everything with one command (details in [`docs/DEPLOYMENT.md`](docs/DEPLOYMENT.md)):
```bash
docker compose -f docker-compose.prod.yml up --build -d
docker compose -f docker-compose.prod.yml logs -f bff   # wait for "Started BffApplication"
```
- App (frontend): <http://localhost/>
- API: <http://localhost/api/...> (nginx proxies to the BFF)
- Mailhog: <http://localhost:8025>

> The first build takes a while (it pulls images including Playwright and compiles the services) — so
> open the browser only after `Started BffApplication`, otherwise nginx will briefly return 502.

### Verifying the system is running (GET in the browser)

- Scenario A: <http://localhost:8082/api/products?name=PlayStation> · dashboard <http://localhost:8083/api/admin/scraper/dashboard>
- Scenario B: <http://localhost/api/products?name=PlayStation>
- Mailhog (both): <http://localhost:8025>

---

## 4. Manual functional verification

> Tip: endpoints that **read** data are GET (can be opened in a browser). Anything
> that **creates** data (registration, login, watchlist, comment…) is POST and
> must be sent from a REST client, e.g. [`api.http`](api.http).

### 4a. Browser checks (GET)

| What | URL |
|---|---|
| Search by name (FRQ1) | <http://localhost:8082/api/products?name=PlayStation> |
| Filter by price + pagination (FRQ6) | <http://localhost:8082/api/products?minPrice=10000&maxPrice=30000&page=0&size=5> |
| Product detail + price history (FRQ2) | <http://localhost:8082/api/products/1> |
| Export price history to CSV (FRQ8) | <http://localhost:8082/api/products/1/export> |
| Scraper dashboard (FRQ7) | <http://localhost:8083/api/admin/scraper/dashboard> |

### 4b. Full walkthrough of the system (via `api.http`)

Open [`api.http`](api.http) and run the requests **in this order**:

1. **Register (via BFF)** — creates a user. The message `Email already in use` means it already exists; continue on.
2. **Login (via BFF)** — **required**: stores the JWT in the `{{bff_token}}` variable, used by subsequent requests.
3. **Search products** — list of products.
4. **Aggregated product detail** — product + comments + `inWatchlist` (aggregated in the BFF).
5. **Add to watchlist** — set `"targetPrice": 25000` (above the current price) so the alert is guaranteed to fire.
6. **Post a comment** (UC5).
7. **Trigger scraper run** — `POST http://localhost:8083/api/admin/scraper/run`.
   The scraper publishes a new price → the catalog stores it and detects a drop below the threshold →
   publishes `price-drops` → notification sends an email.
8. **Open Mailhog** at <http://localhost:8025> — an email *"Price dropped …"* will appear.
   This verifies the entire asynchronous chain.
9. **List my notifications** — notification history (`SENT` / `FAILED` statuses).
10. **Export price history as CSV** — downloads the chart's underlying data (UC6).

### 4c. Where to observe results

- **Mailhog** <http://localhost:8025> — delivered emails.
- **Scraper dashboard** <http://localhost:8083/api/admin/scraper/dashboard> — run counts increase with each `/run`.
- **pgAdmin** <http://localhost:5050> — the `products`, `price_records`, `watchlist_items`, `comments`, `notification_logs` tables get populated. See [§7](#7-viewing-the-database-pgadmin).

> **Admin endpoints via BFF** (`/api/admin/**`) require the `ADMIN` role. A newly
> registered user has the `USER` role. The admin account is created automatically when
> user-service starts: **`admin@pricetracker.local` / `admin123`** — log in with it
> to get a token with the ADMIN role.

### 4d. Test web UI (frontend)

The [`frontend/`](frontend/index.html) directory contains a simple web UI (a single HTML
file, no build step) that covers the entire flow: **registration, login, logging in
as admin**, product search, detail + price history, watchlist, comments,
notifications, and the admin panel (scraper dashboard, triggering scraping, managing shops,
creating a product).

The UI must run on `localhost` (due to CORS). From the project root:

```bash
python -m http.server 5173 --directory frontend
# or: npx serve frontend -l 5173
```

Open <http://localhost:5173>. The UI calls the BFF at <http://localhost:8085>.

Recommended scenario:
1. **Sign in as admin** (button) — `admin@pricetracker.local` / `admin123`.
2. In **Admin · Create product**, create the first product (it gets `id=1`, which the scraper tracks).
3. **Trigger scrape now** → after a moment open the product detail — price history will appear.
4. Register a regular user, add the product to the **watchlist** with a high target price.
5. **Trigger scrape** again → a notification will appear in **My notifications** and in Mailhog.

---

## 5. Data flow

```
Scraper @Scheduled -> PriceUpdateEvent -> kafka topic "price-updates"
                                                |
                                                v
                              Product Catalog consumer
                              - stores PriceRecord
                              - evaluates Watchlist + PriceAlert
                              - if threshold is met -> kafka topic "price-drops"
                                                |
                                                v
                              Notification consumer
                              - looks up the email from user-service
                              - SMTP send + stores NotificationLog
```

---

## 6. Running the tests

The project contains **63 unit tests** covering the core logic (alert evaluation,
watchlist, comments, alerts, CSV export, scraper strategy/orchestrator, notification
status, JWT parsing). They don't need a database or Kafka:

```bash
mvn test -Dtest='*ServiceTest,*FactoryTest,*StrategyTest,*ParserTest,*ControllerTest,*OrchestratorTest' -Dsurefire.failIfNoSpecifiedTests=false
```

(Smoke tests `contextLoads` (`@SpringBootTest`) are excluded here, since they start
the whole Spring context and therefore require the running infrastructure from Step 1.)

---

## 7. Viewing the database (pgAdmin)

1. Open <http://localhost:5050> and log in with `admin@pricetracker.com` / `admin`.
2. **Register Server…** → *Connection* tab:
   - **Host:** `postgres` (container name — **not** `localhost`, since pgAdmin runs inside Docker)
   - **Port:** `5432` (the container's internal port)
   - **Database:** `pricetracker_catalog_db`, **User:** `admin`, **Password:** `adminpassword`
3. Browse: `Servers → … → Databases → pricetracker_catalog_db → Schemas → public → Tables`.

> Note: applications on the host connect to **`localhost:5432`**, while pgAdmin
> (inside Docker) connects to **`postgres:5432`** (the container's internal port).

---

## 8. Troubleshooting

| Symptom | Cause and fix |
|---|---|
| Service crashes with `password authentication failed for user "admin"` | Either another PostgreSQL is running on `localhost:5432` — then change the port mapping in `docker-compose.yml` (e.g. `5433:5432`) and `spring.datasource.url` in the services. Or the volume has stale credentials — reset it: `docker compose down -v && docker compose up -d`. |
| `Port … was already in use` | Another process is holding the port. Change `server.port` in `bff/src/main/resources/application.properties`, `nginx/default.conf`, and the corresponding URL in `api.http` / the frontend. |
| The browser shows a "method 'GET' not supported" error on a POST endpoint | Registration/login/watchlist/comments are **POST** — use `api.http`, not the browser's address bar. |
| A configuration change has no effect | `application.properties` is read at startup — restart the affected service. |
| No email arrived after a scrape / no new price was added | Verify that an **active** shop exists in the `shops` table and that you ran `POST :8083/api/admin/scraper/run`; then check the catalog and notification logs. |

---

## 9. Data model (per service)

- **user-service:** `User(id, email, login, passwordHash, role, createdAt)`
- **product-catalog-service:** `Product`, `Category`, `PriceRecord`, `WatchlistItem`, `Comment`, `PriceAlert`
- **scraper-service:** `Shop(name, baseUrls, isActive, strategyKey, reliabilityScore)`, `ScraperLog(startedAt, finishedAt, successCount, failureCount, blockedCount, status)`
- **notification-service:** `NotificationLog(message, sentAt, status, channel, userId, productId)`

---
