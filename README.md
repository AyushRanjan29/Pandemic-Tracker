# Pandemic Tracker

Healthcare Informatics and Pandemic Tracking Dashboard using Java 17, Spring Boot, JavaFX, and MySQL.

## Modules

- `backend`: Spring Boot REST API, JPA entities, repositories, services, Flyway migration.
- `frontend`: JavaFX desktop dashboard that calls the backend asynchronously.
- `database`: standalone MySQL schema, sample data, and recursive CTE examples.
- `docs`: architecture notes and project structure.

## Prerequisites

- Java 17+
- Maven 3.9+
- MySQL 8+

## Database Setup

Start your local MySQL server on your PC. The backend default connection is:

```text
jdbc:mysql://localhost:3306/pandemic_tracker
username: root
password: empty
```

If your MySQL root account has a password, set it before starting the backend:

```bash
set DB_PASSWORD=your_mysql_password
```

You can also use a dedicated user:

```sql
CREATE DATABASE IF NOT EXISTS pandemic_tracker;
CREATE USER IF NOT EXISTS 'pandemic_app'@'localhost' IDENTIFIED BY 'pandemic_app';
GRANT ALL PRIVILEGES ON pandemic_tracker.* TO 'pandemic_app'@'localhost';
FLUSH PRIVILEGES;
```

Then run the backend with:

```bash
set DB_USERNAME=pandemic_app
set DB_PASSWORD=pandemic_app
```

Flyway creates the tables and inserts demo data automatically when the backend starts.

If you want to run SQL manually instead of Flyway:

```bash
mysql -u root -p pandemic_tracker < database/schema.sql
mysql -u root -p pandemic_tracker < database/sample-data.sql
```

## Run Backend

```bash
cd backend
mvn spring-boot:run
```

Default API base URL: `http://localhost:8080/api/v1`

Override database settings with environment variables:

```bash
set DB_URL=jdbc:mysql://localhost:3306/pandemic_tracker?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
set DB_USERNAME=root
set DB_PASSWORD=your_mysql_password
```

## Run Frontend

Start the backend first, then:

```bash
cd frontend
mvn javafx:run
```

The JavaFX client defaults to `http://localhost:8080/api/v1`. Override with:

```bash
set PANDEMIC_API_BASE_URL=http://localhost:8080/api/v1
```

## Useful Endpoint

```http
GET /api/v1/resource-optimization/states/{stateId}?days=14
```

Returns city-level infection pressure, hospital bed capacity, and vaccine availability for a state.

## Manual Entry APIs

The JavaFX app uses these endpoints behind the add/edit dialogs:

```http
GET  /api/v1/locations?type=STATE
POST /api/v1/manual-entry/city-snapshots
PUT  /api/v1/manual-entry/city-snapshots/{cityId}
```

Restart the backend after adding new Flyway migrations so pending schema or seed changes are applied.
