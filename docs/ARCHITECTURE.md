# Architecture

## Database Design

The `location` table uses an adjacency-list hierarchy through `parent_id`. Hospitals are represented as `LocationType.HOSPITAL` records under cities, which keeps geographic traversal consistent:

```text
COUNTRY -> STATE -> CITY -> HOSPITAL
```

This shape supports recursive SQL without needing schema changes when administrative depth grows. The schema uses:

- Primary keys on every core entity.
- Foreign keys for location, strain, inventory, and infection-source integrity.
- Check constraints for non-negative medical counts and valid risk/type values.
- Time-descending indexes for infection and inventory trend queries.

## Outbreak Path Tracing

`infection_log.source_infection_log_id` captures the most likely upstream infection event for an outbreak observation. The script in `database/recursive-spread-path.sql` starts from the latest target city event and recursively walks backward until it reaches a record with no source, producing the suspected origin and every intermediate hop.

## Backend Layers

The Spring Boot backend is organized by responsibility:

- `domain`: JPA entities and enums.
- `repository`: Spring Data access and native SQL projections.
- `service`: transaction boundaries, validation, and mapping to response DTOs.
- `controller`: REST endpoints and HTTP concerns.

The resource optimization endpoint intentionally uses a native MySQL 8 query because it combines recursive hierarchy traversal, latest inventory snapshots, time-window infection aggregation, and scoring in one read-optimized operation.

## Resource Optimization Scoring

For each city in a state, the backend returns:

- New cases in the selected time window.
- Latest active cases.
- Latest available hospital beds, ICU beds, ventilators, and oxygen cylinders.
- Latest non-expired vaccine doses.
- Cases per 100,000 residents.
- Bed pressure, calculated as active cases divided by available beds.
- A risk label: `STABLE`, `ELEVATED`, `HIGH`, or `CRITICAL`.

These thresholds are starter policy values. In production, they should move to a configuration table so public health administrators can tune them without redeploying the application.

## JavaFX Integration Strategy

The JavaFX client communicates with the backend through `java.net.http.HttpClient.sendAsync`. This prevents network and JSON parsing work from blocking the JavaFX Application Thread.

The UI flow is:

1. User triggers refresh.
2. Controls are disabled and a progress indicator is shown.
3. `PandemicApiClient` sends the API request asynchronously.
4. The completion handler uses `Platform.runLater(...)` for table and status-label updates.
5. Errors are surfaced in the status bar without freezing the window.

For large production dashboards, add pagination or server-side filtering, cache stable reference data such as locations and strains, and use scheduled refreshes with cancellation so stale API calls cannot overwrite newer results.
