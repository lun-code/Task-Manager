# Task Manager

A full-stack task management application built with **Spring Boot** and **Angular**, featuring JWT authentication, email verification, and a clean dark UI.

**Live demo:** [task-manager-nto7.vercel.app](https://task-manager-nto7.vercel.app)

---

## Tech Stack

### Backend
- Java 21 · Spring Boot 3.4 · Spring Security · Spring Data JPA
- PostgreSQL · JWT (jjwt) · Swagger / OpenAPI
- Email verification via [Brevo](https://www.brevo.com/)
- Docker · Deployed on [Railway](https://railway.app/)

### Frontend
- Angular 21 · TypeScript · Tailwind CSS v4
- Reactive Forms · HTTP Interceptors · Route Guards
- Vitest · Deployed on [Vercel](https://vercel.com/)

### Testing
- **Unit tests** — JUnit 5 + Mockito (services and controllers)
- **Integration tests** — Testcontainers with a real PostgreSQL instance

---

## Features

- **Auth** — register, email verification, login/logout with JWT
- **Tasks** — create, edit, delete, toggle completion; paginated list with filtering by status and category
- **Categories** — create, rename, and delete (with conflict protection if a category is in use)
- **Security** — every resource is scoped to the authenticated user; no data leaks between accounts

---

## Project Structure

```
.
├── task-manager-api/          # Spring Boot backend
│   └── src/
│       ├── main/java/…
│       │   ├── config/        # Security, JWT filter, app config
│       │   ├── controller/    # REST controllers
│       │   ├── dto/           # Request / response records
│       │   ├── entity/        # JPA entities
│       │   ├── exception/     # Global exception handler
│       │   ├── repository/    # Spring Data repositories
│       │   ├── service/       # Business logic
│       │   └── specification/ # Dynamic query filters
│       └── test/java/…        # Unit + integration tests
│
├── task-manager-frontend/     # Angular frontend
│   └── src/app/
│       ├── core/              # Services, interceptors, guards, models
│       ├── features/          # Auth, tasks, categories pages
│       └── shared/            # Navbar and reusable components
│
├── docker-compose.yml
└── Dockerfile
```

---

## API Endpoints

### Auth — `/api/auth`
| Method | Path | Description |
|--------|------|-------------|
| POST | `/signup` | Register a new user |
| POST | `/login` | Log in, returns JWT |
| GET | `/me` | Current user info |
| GET | `/verify?token=…` | Verify email address |

### Tasks — `/api/tasks` *(auth required)*
| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | List tasks (paginated, filterable) |
| GET | `/{id}` | Get a task by ID |
| POST | `/` | Create a task |
| PATCH | `/{id}` | Partially update a task |
| DELETE | `/{id}` | Delete a task |

Query params for `GET /`: `completed`, `categoryId`, `page`, `size`

### Categories — `/api/categories` *(auth required)*
| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | List all categories |
| GET | `/{id}` | Get a category by ID |
| POST | `/` | Create a category |
| PATCH | `/{id}` | Rename a category |
| DELETE | `/{id}` | Delete a category |

---

## Running Locally

### Prerequisites
- Java 21
- Node.js 20+
- Docker (for the database)

### Backend

1. Start a PostgreSQL container:
   ```bash
   docker run -d \
     --name task-manager-db \
     -e POSTGRES_DB=task-manager-db \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5433:5432 \
     postgres:15-alpine
   ```

2. Create `task-manager-api/src/main/resources/application-dev.properties` (already in repo), and set the required env vars:
   ```
   DB_USER=postgres
   DB_PASSWORD=postgres
   MAIL_USERNAME=your@gmail.com
   MAIL_PASSWORD=your-app-password
   BREVO_API_KEY=your-brevo-key
   ```

3. Run the application with the `dev` profile:
   ```bash
   cd task-manager-api
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   The API will be available at `http://localhost:8080`.  
   Swagger UI: `http://localhost:8080/swagger-ui.html`

### Frontend

```bash
cd task-manager-frontend
npm install
ng serve
```

The app will be available at `http://localhost:4200`.

---

## Running with Docker Compose

Create a `.env` file in the project root:

```env
POSTGRES_DB=task-manager-db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# JWT
JWT_SECRET=your-base64-secret

# Mail (Brevo)
SPRING_MAIL_HOST=smtp-relay.brevo.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your@email.com
SPRING_MAIL_PASSWORD=your-smtp-key
BREVO_API_KEY=your-brevo-api-key

# PostgreSQL connection (used by Spring in prod profile)
PGHOST=db
PGPORT=5432
PGDATABASE=task-manager-db
PGUSER=postgres
PGPASSWORD=postgres
```

Then start everything:

```bash
docker-compose up --build
```

---

## Running Tests

```bash
# Backend — unit tests only (no Docker needed)
cd task-manager-api
./mvnw test

# Backend — all tests including integration (Docker required for Testcontainers)
./mvnw verify

# Frontend
cd task-manager-frontend
ng test
```

---

## Deployment

| Layer | Platform | Notes |
|-------|----------|-------|
| Backend | Railway | Dockerfile at repo root; env vars set in Railway dashboard |
| Frontend | Vercel | `ng build --configuration production`; `vercel.json` handles SPA routing |
| Database | Railway (Postgres plugin) | Connection injected via `PG*` env vars |
