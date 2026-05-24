CREATE TABLE location (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    type VARCHAR(20) NOT NULL,
    parent_id BIGINT REFERENCES location(id) ON DELETE RESTRICT,
    latitude NUMERIC(9, 6),
    longitude NUMERIC(9, 6),
    population BIGINT CHECK (population IS NULL OR population >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_location_type CHECK (type IN ('COUNTRY', 'STATE', 'CITY', 'HOSPITAL')),
    CONSTRAINT chk_location_parent CHECK (
        (type = 'COUNTRY' AND parent_id IS NULL)
        OR (type <> 'COUNTRY' AND parent_id IS NOT NULL)
    ),
    CONSTRAINT uq_location_parent_name_type UNIQUE (parent_id, name, type)
);

CREATE INDEX idx_location_parent_id ON location(parent_id);
CREATE INDEX idx_location_type ON location(type);
CREATE UNIQUE INDEX uq_location_country_name ON location(name) WHERE parent_id IS NULL AND type = 'COUNTRY';

CREATE TABLE virus_strain (
    id BIGSERIAL PRIMARY KEY,
    who_label VARCHAR(80) NOT NULL,
    pango_lineage VARCHAR(80) NOT NULL,
    common_name VARCHAR(120),
    first_detected_on DATE,
    risk_level VARCHAR(20) NOT NULL DEFAULT 'MONITORING',
    transmissibility_factor NUMERIC(5, 2) NOT NULL DEFAULT 1.00 CHECK (transmissibility_factor > 0),
    immune_escape_score NUMERIC(5, 2) NOT NULL DEFAULT 0.00 CHECK (immune_escape_score >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_virus_strain_who_label UNIQUE (who_label),
    CONSTRAINT uq_virus_strain_pango_lineage UNIQUE (pango_lineage),
    CONSTRAINT chk_virus_risk_level CHECK (risk_level IN ('MONITORING', 'ELEVATED', 'HIGH', 'CRITICAL'))
);

CREATE TABLE infection_log (
    id BIGSERIAL PRIMARY KEY,
    location_id BIGINT NOT NULL REFERENCES location(id) ON DELETE RESTRICT,
    strain_id BIGINT NOT NULL REFERENCES virus_strain(id) ON DELETE RESTRICT,
    observed_at TIMESTAMPTZ NOT NULL,
    new_cases INTEGER NOT NULL CHECK (new_cases >= 0),
    active_cases INTEGER NOT NULL CHECK (active_cases >= 0),
    recoveries INTEGER NOT NULL DEFAULT 0 CHECK (recoveries >= 0),
    deaths INTEGER NOT NULL DEFAULT 0 CHECK (deaths >= 0),
    test_count INTEGER CHECK (test_count IS NULL OR test_count >= 0),
    positivity_rate NUMERIC(5, 2) CHECK (positivity_rate IS NULL OR positivity_rate BETWEEN 0 AND 100),
    source_infection_log_id BIGINT REFERENCES infection_log(id) ON DELETE SET NULL,
    source_confidence NUMERIC(5, 2) CHECK (source_confidence IS NULL OR source_confidence BETWEEN 0 AND 100),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_infection_log_location_strain_time UNIQUE (location_id, strain_id, observed_at),
    CONSTRAINT chk_infection_source_not_self CHECK (source_infection_log_id IS NULL OR source_infection_log_id <> id)
);

CREATE INDEX idx_infection_log_location_time ON infection_log(location_id, observed_at DESC);
CREATE INDEX idx_infection_log_strain_time ON infection_log(strain_id, observed_at DESC);
CREATE INDEX idx_infection_log_source ON infection_log(source_infection_log_id);

CREATE TABLE hospital_inventory (
    id BIGSERIAL PRIMARY KEY,
    hospital_location_id BIGINT NOT NULL REFERENCES location(id) ON DELETE RESTRICT,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    total_beds INTEGER NOT NULL CHECK (total_beds >= 0),
    available_beds INTEGER NOT NULL CHECK (available_beds >= 0),
    icu_beds INTEGER NOT NULL DEFAULT 0 CHECK (icu_beds >= 0),
    available_icu_beds INTEGER NOT NULL DEFAULT 0 CHECK (available_icu_beds >= 0),
    ventilators INTEGER NOT NULL DEFAULT 0 CHECK (ventilators >= 0),
    available_ventilators INTEGER NOT NULL DEFAULT 0 CHECK (available_ventilators >= 0),
    oxygen_cylinders INTEGER NOT NULL DEFAULT 0 CHECK (oxygen_cylinders >= 0),
    available_oxygen_cylinders INTEGER NOT NULL DEFAULT 0 CHECK (available_oxygen_cylinders >= 0),
    updated_by VARCHAR(120) NOT NULL DEFAULT 'system',
    CONSTRAINT chk_beds_available CHECK (available_beds <= total_beds),
    CONSTRAINT chk_icu_available CHECK (available_icu_beds <= icu_beds),
    CONSTRAINT chk_ventilators_available CHECK (available_ventilators <= ventilators),
    CONSTRAINT chk_oxygen_available CHECK (available_oxygen_cylinders <= oxygen_cylinders),
    CONSTRAINT uq_hospital_inventory_snapshot UNIQUE (hospital_location_id, recorded_at)
);

CREATE INDEX idx_hospital_inventory_hospital_time ON hospital_inventory(hospital_location_id, recorded_at DESC);

CREATE TABLE vaccine_inventory (
    id BIGSERIAL PRIMARY KEY,
    hospital_location_id BIGINT NOT NULL REFERENCES location(id) ON DELETE RESTRICT,
    vaccine_name VARCHAR(120) NOT NULL,
    manufacturer VARCHAR(120),
    dose_count INTEGER NOT NULL CHECK (dose_count >= 0),
    reserved_dose_count INTEGER NOT NULL DEFAULT 0 CHECK (reserved_dose_count >= 0),
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_on DATE,
    updated_by VARCHAR(120) NOT NULL DEFAULT 'system',
    CONSTRAINT chk_vaccine_reserved CHECK (reserved_dose_count <= dose_count),
    CONSTRAINT uq_vaccine_inventory_snapshot UNIQUE (hospital_location_id, vaccine_name, recorded_at)
);

CREATE INDEX idx_vaccine_inventory_hospital_time ON vaccine_inventory(hospital_location_id, recorded_at DESC);
CREATE INDEX idx_vaccine_inventory_expiry ON vaccine_inventory(expires_on);
