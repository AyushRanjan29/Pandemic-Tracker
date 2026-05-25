INSERT IGNORE INTO location (id, name, type, parent_id, latitude, longitude, population) VALUES
    (101, 'Telangana', 'STATE', 1, 17.849591, 79.115166, 38157000),
    (102, 'Tamil Nadu', 'STATE', 1, 11.127123, 78.656891, 76860000),
    (103, 'Kerala', 'STATE', 1, 10.850516, 76.271080, 35776000),
    (104, 'Delhi', 'STATE', 1, 28.704060, 77.102493, 32941000),
    (105, 'Hyderabad', 'CITY', 101, 17.385044, 78.486671, 10534000),
    (106, 'Warangal', 'CITY', 101, 17.968901, 79.594055, 1000000),
    (107, 'Chennai', 'CITY', 102, 13.082680, 80.270721, 11503000),
    (108, 'Coimbatore', 'CITY', 102, 11.016844, 76.955833, 2935000),
    (109, 'Kochi', 'CITY', 103, 9.931233, 76.267303, 2120000),
    (110, 'Thiruvananthapuram', 'CITY', 103, 8.524139, 76.936638, 1680000),
    (111, 'New Delhi', 'CITY', 104, 28.613939, 77.209023, 33807000),
    (112, 'Pune', 'CITY', 3, 18.520430, 73.856743, 7340000),
    (113, 'Nagpur', 'CITY', 3, 21.145800, 79.088158, 2940000),
    (114, 'Hyderabad Institute of Virology Care', 'HOSPITAL', 105, 17.420000, 78.470000, NULL),
    (115, 'Warangal Public Health Hospital', 'HOSPITAL', 106, 17.980000, 79.600000, NULL),
    (116, 'Chennai Central Medical College', 'HOSPITAL', 107, 13.060000, 80.250000, NULL),
    (117, 'Coimbatore Regional Hospital', 'HOSPITAL', 108, 11.020000, 76.970000, NULL),
    (118, 'Kochi Lakeside Medical Center', 'HOSPITAL', 109, 9.950000, 76.280000, NULL),
    (119, 'Trivandrum Government Hospital', 'HOSPITAL', 110, 8.510000, 76.930000, NULL),
    (120, 'Delhi National Hospital', 'HOSPITAL', 111, 28.630000, 77.210000, NULL),
    (121, 'Pune Biosecurity Hospital', 'HOSPITAL', 112, 18.530000, 73.860000, NULL),
    (122, 'Nagpur Infectious Disease Center', 'HOSPITAL', 113, 21.150000, 79.090000, NULL);

INSERT IGNORE INTO infection_log (id, location_id, strain_id, observed_at, new_cases, active_cases, recoveries, deaths, test_count, positivity_rate, source_infection_log_id, source_confidence) VALUES
    (101, 105, 1, '2026-05-13 08:00:00', 64, 172, 18, 1, 1300, 4.92, 1, 74.00),
    (102, 106, 1, '2026-05-16 08:00:00', 21, 63, 8, 0, 520, 4.04, 101, 67.00),
    (103, 107, 1, '2026-05-12 08:00:00', 112, 318, 33, 3, 2100, 5.33, 1, 81.00),
    (104, 108, 1, '2026-05-17 08:00:00', 38, 109, 11, 1, 820, 4.63, 103, 72.00),
    (105, 109, 1, '2026-05-18 08:00:00', 24, 74, 9, 0, 610, 3.93, 103, 64.00),
    (106, 110, 1, '2026-05-19 08:00:00', 19, 58, 7, 0, 500, 3.80, 105, 61.00),
    (107, 111, 1, '2026-05-15 08:00:00', 146, 440, 51, 5, 2600, 5.62, 1, 79.00),
    (108, 112, 1, '2026-05-16 08:00:00', 71, 206, 19, 1, 1500, 4.73, 1, 70.00),
    (109, 113, 1, '2026-05-20 08:00:00', 44, 132, 13, 1, 940, 4.68, 108, 66.00),
    (110, 105, 1, '2026-05-23 08:00:00', 88, 238, 22, 2, 1750, 5.03, 101, 84.00),
    (111, 107, 1, '2026-05-23 08:00:00', 134, 366, 41, 4, 2400, 5.58, 103, 86.00),
    (112, 111, 1, '2026-05-23 08:00:00', 161, 502, 56, 6, 2900, 5.55, 107, 88.00);

INSERT IGNORE INTO hospital_inventory (hospital_location_id, recorded_at, total_beds, available_beds, icu_beds, available_icu_beds, ventilators, available_ventilators, oxygen_cylinders, available_oxygen_cylinders, updated_by) VALUES
    (114, '2026-05-24 07:00:00', 420, 58, 60, 9, 42, 8, 310, 128, 'ops-admin'),
    (115, '2026-05-24 07:00:00', 180, 54, 22, 7, 14, 5, 130, 84, 'ops-admin'),
    (116, '2026-05-24 07:00:00', 520, 64, 88, 10, 70, 9, 420, 150, 'ops-admin'),
    (117, '2026-05-24 07:00:00', 260, 76, 32, 11, 20, 8, 210, 120, 'ops-admin'),
    (118, '2026-05-24 07:00:00', 230, 81, 28, 12, 18, 8, 170, 118, 'ops-admin'),
    (119, '2026-05-24 07:00:00', 210, 69, 24, 9, 16, 6, 160, 94, 'ops-admin'),
    (120, '2026-05-24 07:00:00', 760, 72, 130, 14, 96, 11, 560, 166, 'ops-admin'),
    (121, '2026-05-24 07:00:00', 360, 92, 52, 17, 38, 14, 280, 186, 'ops-admin'),
    (122, '2026-05-24 07:00:00', 280, 73, 36, 10, 24, 8, 210, 116, 'ops-admin');

INSERT IGNORE INTO vaccine_inventory (hospital_location_id, vaccine_name, manufacturer, dose_count, reserved_dose_count, recorded_at, expires_on, updated_by) VALUES
    (114, 'PanVax Booster', 'National Biologics', 980, 140, '2026-05-24 07:00:00', '2026-11-30', 'pharmacy'),
    (115, 'PanVax Booster', 'National Biologics', 540, 60, '2026-05-24 07:00:00', '2026-12-15', 'pharmacy'),
    (116, 'PanVax Booster', 'National Biologics', 1120, 220, '2026-05-24 07:00:00', '2026-10-15', 'pharmacy'),
    (117, 'PanVax Booster', 'National Biologics', 780, 80, '2026-05-24 07:00:00', '2026-12-20', 'pharmacy'),
    (118, 'PanVax Booster', 'National Biologics', 690, 75, '2026-05-24 07:00:00', '2026-11-20', 'pharmacy'),
    (119, 'PanVax Booster', 'National Biologics', 610, 65, '2026-05-24 07:00:00', '2026-12-05', 'pharmacy'),
    (120, 'PanVax Booster', 'National Biologics', 1350, 310, '2026-05-24 07:00:00', '2026-09-30', 'pharmacy'),
    (121, 'PanVax Booster', 'National Biologics', 890, 95, '2026-05-24 07:00:00', '2026-11-10', 'pharmacy'),
    (122, 'PanVax Booster', 'National Biologics', 760, 90, '2026-05-24 07:00:00', '2026-12-18', 'pharmacy');

ALTER TABLE location AUTO_INCREMENT = 201;
ALTER TABLE infection_log AUTO_INCREMENT = 201;
