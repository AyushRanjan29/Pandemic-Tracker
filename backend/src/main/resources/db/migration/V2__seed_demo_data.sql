INSERT INTO location (id, name, type, parent_id, latitude, longitude, population) VALUES
    (1, 'India', 'COUNTRY', NULL, 20.593684, 78.962880, 1428600000),
    (2, 'Karnataka', 'STATE', 1, 15.317277, 75.713890, 67562686),
    (3, 'Maharashtra', 'STATE', 1, 19.751480, 75.713888, 123144223),
    (4, 'Bengaluru', 'CITY', 2, 12.971599, 77.594566, 13608000),
    (5, 'Mysuru', 'CITY', 2, 12.295810, 76.639381, 1280000),
    (6, 'Mumbai', 'CITY', 3, 19.076090, 72.877426, 21673000),
    (7, 'CMR General Hospital', 'HOSPITAL', 4, 12.934000, 77.610000, NULL),
    (8, 'Bengaluru City Medical Center', 'HOSPITAL', 4, 12.960000, 77.580000, NULL),
    (9, 'Mysuru District Hospital', 'HOSPITAL', 5, 12.310000, 76.650000, NULL),
    (10, 'Mumbai Central Hospital', 'HOSPITAL', 6, 19.082000, 72.879000, NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO virus_strain (id, who_label, pango_lineage, common_name, first_detected_on, risk_level, transmissibility_factor, immune_escape_score) VALUES
    (1, 'Omicron-XA', 'XA.1', 'Omicron XA', '2026-01-12', 'ELEVATED', 1.42, 0.35)
ON CONFLICT (id) DO NOTHING;

INSERT INTO infection_log (id, location_id, strain_id, observed_at, new_cases, active_cases, recoveries, deaths, test_count, positivity_rate, source_infection_log_id, source_confidence) VALUES
    (1, 6, 1, '2026-05-10 08:00:00+05:30', 47, 120, 12, 1, 900, 5.22, NULL, NULL),
    (2, 4, 1, '2026-05-14 08:00:00+05:30', 28, 88, 9, 0, 700, 4.00, 1, 82.50),
    (3, 5, 1, '2026-05-18 08:00:00+05:30', 16, 49, 5, 0, 420, 3.81, 2, 76.00),
    (4, 4, 1, '2026-05-23 08:00:00+05:30', 54, 144, 14, 1, 1100, 4.91, 2, 88.00)
ON CONFLICT (id) DO NOTHING;

INSERT INTO hospital_inventory (hospital_location_id, recorded_at, total_beds, available_beds, icu_beds, available_icu_beds, ventilators, available_ventilators, oxygen_cylinders, available_oxygen_cylinders, updated_by) VALUES
    (7, '2026-05-24 07:00:00+05:30', 300, 82, 42, 11, 30, 9, 250, 180, 'ops-admin'),
    (8, '2026-05-24 07:00:00+05:30', 220, 44, 30, 6, 18, 5, 180, 104, 'ops-admin'),
    (9, '2026-05-24 07:00:00+05:30', 160, 67, 18, 8, 12, 6, 120, 88, 'ops-admin'),
    (10, '2026-05-24 07:00:00+05:30', 480, 38, 80, 7, 60, 8, 320, 96, 'ops-admin')
ON CONFLICT DO NOTHING;

INSERT INTO vaccine_inventory (hospital_location_id, vaccine_name, manufacturer, dose_count, reserved_dose_count, recorded_at, expires_on, updated_by) VALUES
    (7, 'PanVax Booster', 'National Biologics', 1200, 160, '2026-05-24 07:00:00+05:30', '2026-10-31', 'pharmacy'),
    (8, 'PanVax Booster', 'National Biologics', 640, 80, '2026-05-24 07:00:00+05:30', '2026-11-15', 'pharmacy'),
    (9, 'PanVax Booster', 'National Biologics', 720, 50, '2026-05-24 07:00:00+05:30', '2026-12-01', 'pharmacy'),
    (10, 'PanVax Booster', 'National Biologics', 360, 70, '2026-05-24 07:00:00+05:30', '2026-09-30', 'pharmacy')
ON CONFLICT DO NOTHING;

SELECT setval('location_id_seq', (SELECT max(id) FROM location));
SELECT setval('virus_strain_id_seq', (SELECT max(id) FROM virus_strain));
SELECT setval('infection_log_id_seq', (SELECT max(id) FROM infection_log));
