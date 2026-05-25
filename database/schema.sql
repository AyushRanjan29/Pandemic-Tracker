CREATE TABLE location (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(160) NOT NULL,
    type VARCHAR(20) NOT NULL,
    parent_id BIGINT,
    latitude DECIMAL(9, 6),
    longitude DECIMAL(9, 6),
    population BIGINT CHECK (population IS NULL OR population >= 0),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_location_parent FOREIGN KEY (parent_id) REFERENCES location(id) ON DELETE RESTRICT,
    CONSTRAINT chk_location_type CHECK (type IN ('COUNTRY', 'STATE', 'CITY', 'HOSPITAL')),
    CONSTRAINT chk_location_parent CHECK (
        (type = 'COUNTRY' AND parent_id IS NULL)
        OR (type <> 'COUNTRY' AND parent_id IS NOT NULL)
    ),
    CONSTRAINT uq_location_parent_name_type UNIQUE (parent_id, name, type)
);

CREATE INDEX idx_location_parent_id ON location(parent_id);
CREATE INDEX idx_location_type ON location(type);

CREATE TABLE virus_strain (
    id BIGINT NOT NULL AUTO_INCREMENT,
    who_label VARCHAR(80) NOT NULL,
    pango_lineage VARCHAR(80) NOT NULL,
    common_name VARCHAR(120),
    first_detected_on DATE,
    risk_level VARCHAR(20) NOT NULL DEFAULT 'MONITORING',
    transmissibility_factor DECIMAL(5, 2) NOT NULL DEFAULT 1.00 CHECK (transmissibility_factor > 0),
    immune_escape_score DECIMAL(5, 2) NOT NULL DEFAULT 0.00 CHECK (immune_escape_score >= 0),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uq_virus_strain_who_label UNIQUE (who_label),
    CONSTRAINT uq_virus_strain_pango_lineage UNIQUE (pango_lineage),
    CONSTRAINT chk_virus_risk_level CHECK (risk_level IN ('MONITORING', 'ELEVATED', 'HIGH', 'CRITICAL'))
);

CREATE TABLE infection_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    location_id BIGINT NOT NULL,
    strain_id BIGINT NOT NULL,
    observed_at DATETIME(6) NOT NULL,
    new_cases INT NOT NULL CHECK (new_cases >= 0),
    active_cases INT NOT NULL CHECK (active_cases >= 0),
    recoveries INT NOT NULL DEFAULT 0 CHECK (recoveries >= 0),
    deaths INT NOT NULL DEFAULT 0 CHECK (deaths >= 0),
    test_count INT CHECK (test_count IS NULL OR test_count >= 0),
    positivity_rate DECIMAL(5, 2) CHECK (positivity_rate IS NULL OR positivity_rate BETWEEN 0 AND 100),
    source_infection_log_id BIGINT,
    source_confidence DECIMAL(5, 2) CHECK (source_confidence IS NULL OR source_confidence BETWEEN 0 AND 100),
    notes TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_infection_log_location FOREIGN KEY (location_id) REFERENCES location(id) ON DELETE RESTRICT,
    CONSTRAINT fk_infection_log_strain FOREIGN KEY (strain_id) REFERENCES virus_strain(id) ON DELETE RESTRICT,
    CONSTRAINT fk_infection_log_source FOREIGN KEY (source_infection_log_id) REFERENCES infection_log(id) ON DELETE SET NULL,
    CONSTRAINT uq_infection_log_location_strain_time UNIQUE (location_id, strain_id, observed_at),
    CONSTRAINT chk_infection_source_not_self CHECK (source_infection_log_id IS NULL OR source_infection_log_id <> id)
);

CREATE INDEX idx_infection_log_location_time ON infection_log(location_id, observed_at DESC);
CREATE INDEX idx_infection_log_strain_time ON infection_log(strain_id, observed_at DESC);
CREATE INDEX idx_infection_log_source ON infection_log(source_infection_log_id);

CREATE TABLE hospital_inventory (
    id BIGINT NOT NULL AUTO_INCREMENT,
    hospital_location_id BIGINT NOT NULL,
    recorded_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    total_beds INT NOT NULL CHECK (total_beds >= 0),
    available_beds INT NOT NULL CHECK (available_beds >= 0),
    icu_beds INT NOT NULL DEFAULT 0 CHECK (icu_beds >= 0),
    available_icu_beds INT NOT NULL DEFAULT 0 CHECK (available_icu_beds >= 0),
    ventilators INT NOT NULL DEFAULT 0 CHECK (ventilators >= 0),
    available_ventilators INT NOT NULL DEFAULT 0 CHECK (available_ventilators >= 0),
    oxygen_cylinders INT NOT NULL DEFAULT 0 CHECK (oxygen_cylinders >= 0),
    available_oxygen_cylinders INT NOT NULL DEFAULT 0 CHECK (available_oxygen_cylinders >= 0),
    updated_by VARCHAR(120) NOT NULL DEFAULT 'system',
    PRIMARY KEY (id),
    CONSTRAINT fk_hospital_inventory_location FOREIGN KEY (hospital_location_id) REFERENCES location(id) ON DELETE RESTRICT,
    CONSTRAINT chk_beds_available CHECK (available_beds <= total_beds),
    CONSTRAINT chk_icu_available CHECK (available_icu_beds <= icu_beds),
    CONSTRAINT chk_ventilators_available CHECK (available_ventilators <= ventilators),
    CONSTRAINT chk_oxygen_available CHECK (available_oxygen_cylinders <= oxygen_cylinders),
    CONSTRAINT uq_hospital_inventory_snapshot UNIQUE (hospital_location_id, recorded_at)
);

CREATE INDEX idx_hospital_inventory_hospital_time ON hospital_inventory(hospital_location_id, recorded_at DESC);

CREATE TABLE vaccine_inventory (
    id BIGINT NOT NULL AUTO_INCREMENT,
    hospital_location_id BIGINT NOT NULL,
    vaccine_name VARCHAR(120) NOT NULL,
    manufacturer VARCHAR(120),
    dose_count INT NOT NULL CHECK (dose_count >= 0),
    reserved_dose_count INT NOT NULL DEFAULT 0 CHECK (reserved_dose_count >= 0),
    recorded_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    expires_on DATE,
    updated_by VARCHAR(120) NOT NULL DEFAULT 'system',
    PRIMARY KEY (id),
    CONSTRAINT fk_vaccine_inventory_location FOREIGN KEY (hospital_location_id) REFERENCES location(id) ON DELETE RESTRICT,
    CONSTRAINT chk_vaccine_reserved CHECK (reserved_dose_count <= dose_count),
    CONSTRAINT uq_vaccine_inventory_snapshot UNIQUE (hospital_location_id, vaccine_name, recorded_at)
);

CREATE INDEX idx_vaccine_inventory_hospital_time ON vaccine_inventory(hospital_location_id, recorded_at DESC);
CREATE INDEX idx_vaccine_inventory_expiry ON vaccine_inventory(expires_on);
