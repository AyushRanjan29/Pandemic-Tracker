# Project Structure

```text
pandemic-tracker/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/pandemictracker/backend/
│       │   ├── PandemicTrackerBackendApplication.java
│       │   ├── config/WebConfig.java
│       │   ├── controller/
│       │   ├── domain/
│       │   ├── dto/
│       │   ├── repository/
│       │   └── service/
│       └── resources/
│           ├── application.yml
│           └── db/migration/V1__create_pandemic_schema.sql
├── frontend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/pandemictracker/frontend/
│       │   ├── PandemicTrackerClientApp.java
│       │   ├── api/
│       │   └── dto/
│       └── resources/styles/dashboard.css
├── database/
│   ├── schema.sql
│   ├── sample-data.sql
│   └── recursive-spread-path.sql
├── docs/
│   ├── ARCHITECTURE.md
│   └── PROJECT_STRUCTURE.md
├── pom.xml
└── README.md
```

## Main Responsibilities

- `Location`: self-referencing administrative hierarchy for country, state, city, and hospital nodes.
- `VirusStrain`: strain metadata, epidemiological risk level, and lineage identifiers.
- `InfectionLog`: time-series case metrics with optional source event linkage for spread tracing.
- `HospitalInventory`: latest and historical hospital resource snapshots.
- `VaccineInventory`: available and reserved doses by hospital and vaccine.
- `ResourceOptimizationService`: read-only orchestration for state-level hotspot and inventory analysis.
- `PandemicTrackerClientApp`: JavaFX dashboard that keeps API calls off the UI thread.
