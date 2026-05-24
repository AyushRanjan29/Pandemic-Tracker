# Pandemic Tracker

Healthcare Informatics and Pandemic Tracking Dashboard using Java 17, Spring Boot, JavaFX, and PostgreSQL.

## Modules

- `backend`: Spring Boot REST API, JPA entities, repositories, services, Flyway migration.
- `frontend`: JavaFX desktop dashboard that calls the backend asynchronously.
- `database`: standalone PostgreSQL schema, sample data, and recursive CTE examples.
- `docs`: architecture notes and project structure.

## Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL 14+

## Database Setup

### Option A: Docker

```bash
docker compose up -d postgres
```

Then start the backend. Flyway will create the tables automatically.
The bundled Flyway migrations also insert demo data so the JavaFX default `State ID = 2` works out of the box.
Later migrations expand the dataset with additional Indian states, cities, hospitals, outbreak chains, and inventories.

### Option B: Local PostgreSQL

Create a database and user, then run the standalone schema if you are not using Flyway:

```sql
CREATE DATABASE pandemic_tracker;
CREATE USER pandemic_app WITH PASSWORD 'pandemic_app';
GRANT ALL PRIVILEGES ON DATABASE pandemic_tracker TO pandemic_app;
```

```bash
psql -U pandemic_app -d pandemic_tracker -f database/schema.sql
psql -U pandemic_app -d pandemic_tracker -f database/sample-data.sql
```

The Spring Boot backend also ships with the same schema and demo seed data as Flyway migrations in `backend/src/main/resources/db/migration`.

## Run Backend

```bash
cd backend
mvn spring-boot:run
```

Default API base URL: `http://localhost:8080/api/v1`

Override database settings with environment variables:

```bash
DB_URL=jdbc:postgresql://localhost:5432/pandemic_tracker
DB_USERNAME=pandemic_app
DB_PASSWORD=pandemic_app
```

## Run Frontend

Start the backend first, then:

```bash
cd frontend
mvn javafx:run
```

The JavaFX client defaults to `http://localhost:8080/api/v1`. Override with:

```bash
PANDEMIC_API_BASE_URL=http://localhost:8080/api/v1
```

## Useful Endpoint

```http
GET /api/v1/resource-optimization/states/{stateId}?days=14
```

Returns city-level infection pressure, hospital bed capacity, and vaccine availability for a state.

## Manual Entry APIs

The JavaFX app uses these endpoints behind the `Add Data` dialog:

```http
GET  /api/v1/locations?type=STATE
POST /api/v1/locations
POST /api/v1/manual-entry/infection-logs
POST /api/v1/manual-entry/hospital-inventories
POST /api/v1/manual-entry/vaccine-inventories
```

Restart the backend after adding new Flyway migrations so pending seed data is applied.
