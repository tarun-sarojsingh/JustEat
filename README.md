# JustEat Food Ordering Application

A simplified full-stack food ordering app built for the Trainee Full-Stack Development assignment:
**React frontend + Spring Boot backend + MySQL, with JWT auth, Docker, and Swagger docs.**

This build covers the core user journeys end-to-end and intentionally leaves out a few
lower-priority extras (see [Scope & what's simplified](#scope--whats-simplified) below) to keep
the codebase easy to read, run, and extend.

## Features implemented

**Customers**
- Register / log in (JWT-based)
- Browse & search restaurants by name or cuisine
- View a restaurant's menu, grouped by category
- Add items to a cart, place an order
- Track order status (polls every 30s: Pending → Preparing → Ready → Completed)
- View past order history

**Restaurant owners**
- Register one restaurant per account and edit its details
- Add, edit, and soft-delete menu items (`is_deleted` flag — order history is preserved)
- View incoming orders and advance their status (forward-only — can't move backwards)

**Cross-cutting**
- Stateless JWT authentication with role-based access control (`CUSTOMER` / `RESTAURANT_OWNER`)
- Swagger / OpenAPI docs auto-generated at `/api-docs` and `/swagger-ui.html`
- 13 JUnit 5 + Mockito unit tests covering the core service layer (exceeds the 10-test minimum)
- Multi-stage Dockerfile for the backend + `docker-compose.yml` bringing up MySQL, backend, and frontend together

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | React 18 (hooks, Context API), React Router, Axios, React-Toastify |
| Backend | Spring Boot 3 (Java 21), Spring Security, Spring Data JPA / Hibernate |
| Database | MySQL 8 (Docker) — falls back to file-based H2 for zero-setup local dev |
| Auth | JWT (jjwt) |
| API docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5 + Mockito |
| Containers | Docker, docker-compose |

## Project structure

```
justeat-app/
├── backend/                  Spring Boot API
│   ├── src/main/java/com/justeat/
│   │   ├── model/             JPA entities (User, Restaurant, MenuItem, Order, OrderItem)
│   │   ├── repository/        Spring Data JPA repositories
│   │   ├── service/           Business logic
│   │   ├── controller/        REST controllers
│   │   ├── security/          JWT util, filter, UserDetailsService
│   │   ├── config/            Security & OpenAPI config
│   │   ├── dto/                Request/response DTOs
│   │   └── exception/          Custom exceptions + global handler
│   ├── src/test/java/...      Unit tests
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                 React SPA
│   └── src/
│       ├── api/               Axios client with JWT interceptor
│       ├── context/            Auth & Cart state (React Context)
│       ├── components/         Navbar, ProtectedRoute
│       └── pages/               Login, Register, Restaurants, Menu, Cart, OrderTracking, OrderHistory, OwnerDashboard
├── docker-compose.yml
├── README.md
└── AI_USAGE.md
```

## Fixed since first draft

Two real bugs were found and fixed after initial testing:

1. **Owners couldn't reliably reach their dashboard.** The frontend was tracking "my
   restaurant" via a `localStorage` key set at registration time instead of asking the
   backend. If that key was ever missing (different browser, cleared storage, etc.), the
   dashboard showed the registration form again even though a restaurant already existed —
   attempting to re-register then failed with a 409 conflict, leaving no way to manage the
   menu. **Fix:** added a real `GET /api/restaurant/mine` endpoint and updated the dashboard
   to use it.
2. **Adding a menu item appeared to fail even when it succeeded.** After a successful
   `POST /api/menu-items`, the dashboard immediately re-fetched the menu via
   `GET /api/restaurants/{id}/menu` — but that endpoint was locked to the `CUSTOMER` role
   only, so the owner's own refresh call got a 403 and the UI showed "Could not add menu
   item." **Fix:** that endpoint (viewing a restaurant's menu) is now open to any
   authenticated user, since owners legitimately need to view their own menu too.
3. **Data was disappearing between runs.** The default local database was in-memory H2,
   which resets on every backend restart — restaurants and menu items registered in one
   session vanished the next time you ran `mvn spring-boot:run`. **Fix:** switched the
   default to a file-based H2 database (`./data/justeat.mv.db`), so local data now persists
   across restarts. Docker/MySQL usage is unaffected.

## Prerequisites

- Java 21
- Node.js 18+
- Maven (or use the included `mvnw` if you add one — this repo assumes Maven is installed)
- Docker + Docker Compose (optional, only needed for the containerised setup)
- MySQL 8 (optional — only if you want to run the backend outside Docker against real MySQL)

## Running locally (fastest — no MySQL install needed)

The backend defaults to a file-based **H2** database, so you can run it immediately and keep data across restarts:

```bash
cd backend
mvn spring-boot:run
```

The API is now available at `http://localhost:8080`. Swagger docs: `http://localhost:8080/swagger-ui.html`.

In a separate terminal:

```bash
cd frontend
npm install
npm start
```

The app opens at `http://localhost:3000`.

## Running with Docker (MySQL + backend + frontend)

```bash
docker-compose up --build
```

This starts:
- MySQL on `localhost:3306`
- Backend on `localhost:8080` (using the `docker` Spring profile, connecting to MySQL)
- Frontend on `localhost:3000`

The Docker profile also seeds demo data on first startup so the UI is not empty:
- Restaurant owner login: `owner` / `Password123!`
- Customer login: `customer` / `Password123!`
- Seeded restaurant: `Spice Route Kitchen`
- Seeded menu items: `Chicken Tikka Masala`, `Paneer Wrap`, `Mango Lassi`

## Environment variables

Copy `.env.example` if you add one, or set these directly:

| Variable | Used by | Default |
|---|---|---|
| `JWT_SECRET` | backend | demo value in `application.properties` — **change in production** |
| `MYSQL_USER` / `MYSQL_PASSWORD` | backend (docker profile) | `justeat` / `justeat` |
| `REACT_APP_API_URL` | frontend | `http://localhost:8080/api` |

## Running tests

```bash
cd backend
mvn test
```

13 tests across `AuthService`, `RestaurantService`, `MenuItemService`, `OrderService`, and `JwtUtil`.

## API overview

Full interactive docs are at `/swagger-ui.html` once the backend is running. Summary:

| Method | Endpoint | Auth |
|---|---|---|
| POST | `/api/auth/register` | none |
| POST | `/api/auth/login` | none |
| GET | `/api/restaurants?query=` | CUSTOMER |
| GET | `/api/restaurants/{id}/menu` | CUSTOMER |
| POST | `/api/orders` | CUSTOMER |
| GET | `/api/orders/{id}` | CUSTOMER |
| GET | `/api/orders/history` | CUSTOMER |
| POST | `/api/restaurant` | RESTAURANT_OWNER |
| PUT | `/api/restaurant/{id}` | RESTAURANT_OWNER |
| POST | `/api/menu-items` | RESTAURANT_OWNER |
| PUT | `/api/menu-items/{id}` | RESTAURANT_OWNER |
| DELETE | `/api/menu-items/{id}` (soft delete) | RESTAURANT_OWNER |
| GET | `/api/orders/incoming` | RESTAURANT_OWNER |
| PUT | `/api/orders/{id}/status` | RESTAURANT_OWNER |

## Scope & what's simplified

To keep this an approachable, easy-to-read trainee submission rather than a sprawling
enterprise codebase, a few things from the original brief were deliberately trimmed or
simplified — flagged here rather than silently dropped:

- **Password reset via email (US 1.2)** — not implemented. Would need an email provider
  (e.g. SES/SendGrid) and a token table; out of scope for this pass.
- **User preferences / favourite cuisines (US 2.6)** — not implemented as its own feature.
  The `user_preferences` table from the design doc was left out; search doesn't yet
  personalise by cuisine.
- **"Deal of the Day" auto-clear at midnight & "Mostly Ordered" scheduled job (US 3.3, 3.4)** —
  the `isTodaysSpecial` / `isPopular` fields exist on `MenuItem`, but the `@Scheduled` jobs that
  would auto-calculate and auto-clear them weren't built. An owner can still toggle
  "Today's Special" manually via the API.
- **Geolocation-based search** — search is by name/cuisine text match only, not by distance.
- **Cart persistence** — the cart lives in React state (Context), so it clears on page refresh
  rather than persisting via localStorage/session.

Everything else in the assignment brief — auth, role-based access, the core ordering flow,
order status tracking with forward-only transitions, soft-deleted menu items, unit tests,
Docker, and Swagger — is implemented and working.

## Notes on the database

By default the app runs against H2 (zero setup). Hibernate's `ddl-auto=update` creates the
schema automatically on first run — there are no separate `.sql` migration files, which keeps
things simple for a project this size. For a production system, a migration tool
(Flyway/Liquibase) would replace `ddl-auto=update`.
