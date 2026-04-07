# Dashboard Application — Design & Implementation Plan

## 1. Overview

A generic, configuration-driven dashboard application that displays monthly aggregated data across multiple domains (plaatsingen, contactmomenten, aanbod, and future domains). The frontend is fully generic — all structure, filters, chart behavior, and table columns are driven by the API. Adding a new dashboard requires only backend changes.

---

## 2. Architecture

```
+-------------------+       HTTPS/JSON        +-------------------+
|                   | <---------------------> |                   |
|  Frontend (SPA)   |                          |   Backend API     |
|  React + Carbon   |                          |   Java / Spring   |
|                   |                          |   Boot             |
+-------------------+                          +--------+----------+
                                                        |
                                                        | JPA / JDBC
                                                        |
                                               +--------+----------+
                                               |                   |
                                               |    PostgreSQL     |
                                               |                   |
                                               +-------------------+
```

### Technology choices

| Layer | Technology | Rationale |
|-------|-----------|-----------|
| Frontend | React 18 + TypeScript | Component model fits config-driven UI; strong typing |
| UI library | Carbon Design System (React) | Already validated in PoC; IBM enterprise look & feel |
| Charts | Chart.js via react-chartjs-2 | Lightweight, proven in PoC, supports stacked bars |
| Backend | Java 21 + Spring Boot 3 | Enterprise standard, strong ecosystem |
| Database | PostgreSQL 16 | Reliable, good aggregation support, JSON operators if needed |
| API spec | OpenAPI 3.0 | Already defined in swagger-api.json |
| Build | Maven (backend), Vite (frontend) | Fast builds, standard tooling |
| Deployment | Docker Compose (dev), Kubernetes (prod) | Containerized, scalable |

---

## 3. Data Model

### 3.1 Database Schema

```sql
-- Shared lookup tables

CREATE TABLE team (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE coach (
    id          BIGSERIAL PRIMARY KEY,
    full_name   VARCHAR(200) NOT NULL,
    team_id     BIGINT REFERENCES team(id)
);

CREATE TABLE inwoner (
    id                  BIGSERIAL PRIMARY KEY,
    full_name           VARCHAR(200) NOT NULL,
    birthdate           DATE,
    administratienummer VARCHAR(20) NOT NULL UNIQUE
);

-- Domain tables

CREATE TABLE plaatsing (
    id          BIGSERIAL PRIMARY KEY,
    inwoner_id  BIGINT NOT NULL REFERENCES inwoner(id),
    coach_id    BIGINT NOT NULL REFERENCES coach(id),
    team_id     BIGINT NOT NULL REFERENCES team(id),
    start_date  DATE NOT NULL,
    type        VARCHAR(100) NOT NULL
);

CREATE TABLE contactmoment (
    id          BIGSERIAL PRIMARY KEY,
    inwoner_id  BIGINT NOT NULL REFERENCES inwoner(id),
    coach_id    BIGINT NOT NULL REFERENCES coach(id),
    team_id     BIGINT NOT NULL REFERENCES team(id),
    date        DATE NOT NULL,
    kanaal      VARCHAR(100) NOT NULL,
    onderwerp   VARCHAR(200) NOT NULL
);

CREATE TABLE aanbod (
    id                  BIGSERIAL PRIMARY KEY,
    inwoner_id          BIGINT NOT NULL REFERENCES inwoner(id),
    coach_id            BIGINT NOT NULL REFERENCES coach(id),
    team_id             BIGINT NOT NULL REFERENCES team(id),
    start_date          DATE NOT NULL,
    aanbodnaam          VARCHAR(200) NOT NULL,
    afsluitreden        VARCHAR(200)
);

-- Indexes for filter and aggregation performance

CREATE INDEX idx_plaatsing_start_date ON plaatsing(start_date);
CREATE INDEX idx_plaatsing_team ON plaatsing(team_id);
CREATE INDEX idx_plaatsing_coach ON plaatsing(coach_id);
CREATE INDEX idx_plaatsing_type ON plaatsing(type);

CREATE INDEX idx_contactmoment_date ON contactmoment(date);
CREATE INDEX idx_contactmoment_team ON contactmoment(team_id);
CREATE INDEX idx_contactmoment_coach ON contactmoment(coach_id);
CREATE INDEX idx_contactmoment_kanaal ON contactmoment(kanaal);

CREATE INDEX idx_aanbod_start_date ON aanbod(start_date);
CREATE INDEX idx_aanbod_team ON aanbod(team_id);
CREATE INDEX idx_aanbod_coach ON aanbod(coach_id);
CREATE INDEX idx_aanbod_naam ON aanbod(aanbodnaam);
```

### 3.2 Domain rules encoded in backend

- Aanbod: records with `afsluitreden IN ('Administratief afgesloten', 'Aanbod afgesloten wegens wijzigen leerroute')` are excluded from all queries.
- Coach-team mapping: a coach belongs to a team, but the recorded team_id on each record reflects the team at time of the event (historical accuracy).

---

## 4. API Design

Full OpenAPI spec: `swagger-api.json`

### 4.1 Endpoints summary

```
GET /v1/dashboards                              → List tabs
GET /v1/dashboards/{id}/config                  → UI configuration
GET /v1/dashboards/{id}/filters?{filters}       → Cascading filter options
GET /v1/dashboards/{id}/summary?{filters}       → Summary card values
GET /v1/dashboards/{id}/chart?{filters}         → Chart series data
GET /v1/dashboards/{id}/details?month=&{filters} → Detail table rows
```

### 4.2 Request flow

1. On app load: `GET /dashboards` → render tabs
2. On tab switch: `GET /config` → build filter dropdowns, chart, summary cards, table headers
3. On initial render and each filter change:
   - Parallel: `GET /filters`, `GET /summary`, `GET /chart`
4. On bar click: `GET /details?month=2026-03&category=Taalcoaching`

### 4.3 Filter mechanics

All data endpoints (`/filters`, `/summary`, `/chart`, `/details`) accept the same filter query parameters: `?year=2026&team=Team+Inburgering&coach=...`

The `/filters` endpoint computes cascading options: for each filter key, it applies all OTHER active filters to determine which values still have data. This prevents users from selecting impossible filter combinations.

---

## 5. Backend Design

### 5.1 Project structure

```
backend/
├── pom.xml
└── src/main/java/nl/commutr/dashboard/
    ├── DashboardApplication.java
    ├── config/
    │   └── DashboardDefinitions.java        # Registry of dashboard configs
    ├── controller/
    │   └── DashboardController.java         # REST endpoints
    ├── model/
    │   ├── entity/                          # JPA entities
    │   │   ├── Plaatsing.java
    │   │   ├── Contactmoment.java
    │   │   ├── Aanbod.java
    │   │   ├── Coach.java
    │   │   ├── Team.java
    │   │   └── Inwoner.java
    │   └── dto/                             # API response objects
    │       ├── DashboardSummaryDto.java
    │       ├── DashboardConfigDto.java
    │       ├── FilterOptionsDto.java
    │       ├── SummaryDataDto.java
    │       ├── ChartDataDto.java
    │       └── DetailDataDto.java
    ├── repository/
    │   ├── PlaatsingRepository.java
    │   ├── ContactmomentRepository.java
    │   └── AanbodRepository.java
    └── service/
        ├── DashboardService.java            # Orchestrates queries per dashboard
        ├── DashboardQueryBuilder.java       # Builds dynamic filtered queries
        └── impl/
            ├── PlaatsingDashboardProvider.java
            ├── ContactmomentDashboardProvider.java
            └── AanbodDashboardProvider.java
```

### 5.2 Dashboard provider pattern

Each dashboard implements a `DashboardProvider` interface:

```java
public interface DashboardProvider {
    String getId();
    DashboardConfigDto getConfig();
    FilterOptionsDto getFilterOptions(Map<String, String> activeFilters);
    SummaryDataDto getSummary(Map<String, String> activeFilters);
    ChartDataDto getChartData(Map<String, String> activeFilters);
    DetailDataDto getDetails(String month, String category, Map<String, String> activeFilters);
}
```

Providers are registered in a `Map<String, DashboardProvider>` at startup. The controller delegates to the correct provider by dashboard ID. Adding a new dashboard = adding a new provider class.

### 5.3 Query strategy

Use Spring Data JPA Specifications (or Querydsl) for dynamic filter composition:

```java
// Pseudocode for AanbodDashboardProvider.getChartData()
Specification<Aanbod> spec = baseSpec()                    // excludes admin-closed records
    .and(yearEquals(filters.get("year")))
    .and(teamEquals(filters.get("team")))
    .and(coachEquals(filters.get("coach")))
    .and(aanbodnaamEquals(filters.get("aanbodnaam")))
    .and(afsluitredenEquals(filters.get("afsluitreden")));

// Aggregate via native query or Criteria API
// GROUP BY EXTRACT(YEAR FROM start_date), EXTRACT(MONTH FROM start_date), aanbodnaam
```

For the chart and summary endpoints, use native SQL with `GROUP BY` for efficiency rather than loading all records into memory.

---

## 6. Frontend Design

### 6.1 Project structure

```
frontend/
├── package.json
├── vite.config.ts
├── tsconfig.json
└── src/
    ├── main.tsx
    ├── App.tsx                              # Tab routing
    ├── api/
    │   └── dashboardApi.ts                  # API client (fetch wrapper)
    ├── types/
    │   └── dashboard.ts                     # TypeScript types from OpenAPI
    ├── components/
    │   ├── DashboardShell.tsx               # Generic dashboard: filters + summary + chart + table
    │   ├── FilterBar.tsx                    # Renders filter dropdowns from config
    │   ├── SummaryCards.tsx                 # Renders summary tiles from config
    │   ├── DashboardChart.tsx              # Renders bar or stacked-bar from config + data
    │   └── DetailTable.tsx                 # Renders detail table from config + data
    └── hooks/
        ├── useDashboardConfig.ts            # Fetches and caches config
        ├── useFilters.ts                    # Manages filter state + fetches options
        └── useDashboardData.ts              # Fetches summary + chart + details
```

### 6.2 Component hierarchy

```
App
└── TabBar (from /dashboards)
    └── DashboardShell (per active tab)
        ├── FilterBar
        │   └── CarbonDropdown (per filter from config)
        ├── SummaryCards
        │   └── CarbonTile (per card from config)
        ├── DashboardChart
        │   └── Chart.js (bar or stacked-bar from config.chart.type)
        └── DetailTable (shown on bar click)
            └── CarbonDataTable (columns from config.detailTable)
```

### 6.3 State management

No global state library needed. Each `DashboardShell` manages its own state:

```typescript
// DashboardShell.tsx
const { config } = useDashboardConfig(dashboardId);
const { filters, setFilter, options } = useFilters(dashboardId, config);
const { summary, chart, loadDetails, details } = useDashboardData(dashboardId, filters);
```

Filter changes trigger parallel fetches for `/filters`, `/summary`, `/chart`. Bar clicks trigger `/details`.

### 6.4 Key behaviors

- **Cascading filters**: on any filter change, fetch new options from `/filters` and update all dropdowns. If a selected value disappears from the options, reset it to "all".
- **Chart click**: on bar click, extract month label (mapped back to `YYYY-MM`) and optionally the series label (for stacked), then call `/details`.
- **Loading states**: use Carbon's `SkeletonText` / `DataTableSkeleton` while fetching.
- **URL state**: sync active tab and filter values to URL query params so dashboards are bookmarkable/shareable.

---

## 7. Implementation Plan

### Phase 1: Project setup (1 sprint)

| Task | Details |
|------|---------|
| 1.1 | Initialize Spring Boot project with Maven, Java 21, Spring Web, Spring Data JPA, PostgreSQL driver |
| 1.2 | Initialize React project with Vite, TypeScript, Carbon Design System, react-chartjs-2 |
| 1.3 | Set up Docker Compose: PostgreSQL + backend + frontend (dev) |
| 1.4 | Set up Flyway for database migrations |
| 1.5 | Create database schema (migration V1) |
| 1.6 | Create seed data script (migration V2) — port test data generation from PoC |
| 1.7 | Generate TypeScript types from OpenAPI spec |

### Phase 2: Backend core (1 sprint)

| Task | Details |
|------|---------|
| 2.1 | Create JPA entities for all tables |
| 2.2 | Create repositories with Spring Data JPA |
| 2.3 | Implement `DashboardProvider` interface |
| 2.4 | Implement `PlaatsingDashboardProvider` — config, filters, summary, chart, details |
| 2.5 | Implement `ContactmomentDashboardProvider` |
| 2.6 | Implement `AanbodDashboardProvider` — including exclusion filter and stacked chart logic |
| 2.7 | Implement `DashboardController` with all 6 endpoints |
| 2.8 | Write integration tests for each endpoint using `@SpringBootTest` + Testcontainers |

### Phase 3: Frontend core (1 sprint)

| Task | Details |
|------|---------|
| 3.1 | Build API client module (`dashboardApi.ts`) |
| 3.2 | Build `App.tsx` with tab bar driven by `/dashboards` |
| 3.3 | Build `DashboardShell` — orchestrates sub-components |
| 3.4 | Build `FilterBar` — renders dropdowns from config, handles cascading |
| 3.5 | Build `SummaryCards` — renders tiles from config + data |
| 3.6 | Build `DashboardChart` — renders bar or stacked-bar based on `config.chart.type` |
| 3.7 | Build `DetailTable` — renders on bar click with columns from config |
| 3.8 | Add loading skeletons and empty states |

### Phase 4: Integration & polish (1 sprint)

| Task | Details |
|------|---------|
| 4.1 | Connect frontend to backend, end-to-end testing |
| 4.2 | URL state sync — tab and filters in query params |
| 4.3 | Responsive layout for smaller screens |
| 4.4 | Error handling — API errors, network failures, empty data |
| 4.5 | Performance: debounce rapid filter changes (300ms), abort in-flight requests on new filter change |
| 4.6 | Accessibility audit — keyboard navigation, ARIA labels, screen reader testing |
| 4.7 | Cross-browser testing (Chrome, Firefox, Safari, Edge) |

### Phase 5: Production readiness (1 sprint)

| Task | Details |
|------|---------|
| 5.1 | Add authentication (integrate with existing identity provider, e.g. Azure AD / Keycloak) |
| 5.2 | Add CORS configuration |
| 5.3 | Add request logging and monitoring (Spring Actuator, structured logging) |
| 5.4 | Add caching headers or server-side caching for config and filter options |
| 5.5 | Dockerfile for backend (multi-stage build) and frontend (nginx) |
| 5.6 | Kubernetes manifests or Helm chart |
| 5.7 | CI/CD pipeline (build, test, lint, Docker push, deploy) |
| 5.8 | Write operational runbook |

---

## 8. Adding a New Dashboard

The entire point of this architecture is that a new dashboard requires zero frontend changes:

1. **Create database table** — new Flyway migration
2. **Create JPA entity and repository**
3. **Implement a new `DashboardProvider`** — define config (filters, chart type, columns) and implement the 5 data methods
4. **Register the provider** — it auto-registers via Spring's component scanning
5. **Done** — the frontend discovers it via `GET /dashboards` and renders it

---

## 9. Data Flow Example

User opens Aanbod tab and selects `year=2026` and `team=Team Inburgering`:

```
Frontend                              Backend                              Database
   |                                     |                                     |
   |-- GET /dashboards/aanbod/config --->|                                     |
   |<-- { filters, chart: stacked-bar, columns... }                            |
   |                                     |                                     |
   |-- GET /filters?year=2026&team=Team+Inburgering -------->|                 |
   |-- GET /summary?year=2026&team=Team+Inburgering -------->|                 |
   |-- GET /chart?year=2026&team=Team+Inburgering ---------->|                 |
   |   (parallel)                        |                                     |
   |                                     |-- SELECT DISTINCT coach             |
   |                                     |   FROM aanbod WHERE year=2026       |
   |                                     |   AND team='Team Inburgering'       |
   |                                     |   AND afsluitreden NOT IN (...)  -->|
   |                                     |<-- [Ahmed, Fatima, Mark, Eva] ------|
   |                                     |                                     |
   |<-- filters: { coach: [...], aanbodnaam: [...], ... }                      |
   |<-- summary: { total: 87, avg: 29, top: 'Feb 2026 (35)' }                 |
   |<-- chart: { labels: [Jan,Feb,Mrt], series: [{Taalcoaching,[5,8,3]},..]}   |
   |                                     |                                     |
   | User clicks "Taalcoaching" in Feb   |                                     |
   |                                     |                                     |
   |-- GET /details?month=2026-02&category=Taalcoaching&year=2026&team=... --->|
   |                                     |-- SELECT * FROM aanbod              |
   |                                     |   WHERE month=2026-02               |
   |                                     |   AND aanbodnaam='Taalcoaching'     |
   |                                     |   AND ... filters ... ------------->|
   |                                     |<-- [rows] --------------------------|
   |<-- { title: "Taalcoaching in Feb 2026 (8)", rows: [...] }                 |
```

---

## 10. Non-functional Requirements

| Requirement | Target |
|-------------|--------|
| Response time | < 500ms for all endpoints (at expected data volumes) |
| Concurrent users | 50+ simultaneous dashboard users |
| Data volume | Up to 100k records per domain table |
| Browser support | Chrome, Firefox, Safari, Edge (latest 2 versions) |
| Accessibility | WCAG 2.1 AA |
| Availability | 99.5% uptime during business hours |
