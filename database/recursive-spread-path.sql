-- Trace a target infection event back to its suspected origin using the
-- source_infection_log_id edge recorded by epidemiology teams.
--
-- Parameters:
--   :target_city_id  - city location id where the outbreak is currently observed
--   :strain_id       - virus strain id

WITH RECURSIVE target_event AS (
    SELECT il.*
    FROM infection_log il
    JOIN location city ON city.id = il.location_id
    WHERE il.location_id = :target_city_id
      AND il.strain_id = :strain_id
      AND city.type = 'CITY'
    ORDER BY il.observed_at DESC
    LIMIT 1
),
spread_path AS (
    SELECT
        il.id AS infection_log_id,
        il.source_infection_log_id,
        il.location_id,
        il.strain_id,
        il.observed_at,
        il.new_cases,
        il.active_cases,
        il.source_confidence,
        0 AS hop_distance_from_target,
        ARRAY[il.id] AS visited_log_ids
    FROM target_event il

    UNION ALL

    SELECT
        parent.id,
        parent.source_infection_log_id,
        parent.location_id,
        parent.strain_id,
        parent.observed_at,
        parent.new_cases,
        parent.active_cases,
        child.source_confidence,
        child.hop_distance_from_target + 1,
        child.visited_log_ids || parent.id
    FROM spread_path child
    JOIN infection_log parent ON parent.id = child.source_infection_log_id
    WHERE NOT parent.id = ANY(child.visited_log_ids)
)
SELECT
    sp.hop_distance_from_target,
    country.name AS country,
    state.name AS state,
    city.name AS city,
    vs.who_label,
    vs.pango_lineage,
    sp.observed_at,
    sp.new_cases,
    sp.active_cases,
    sp.source_confidence,
    CASE
        WHEN sp.source_infection_log_id IS NULL THEN 'SUSPECTED_ORIGIN'
        WHEN sp.hop_distance_from_target = 0 THEN 'TARGET_CITY_EVENT'
        ELSE 'UPSTREAM_TRANSMISSION_EVENT'
    END AS path_role
FROM spread_path sp
JOIN location city ON city.id = sp.location_id AND city.type = 'CITY'
JOIN location state ON state.id = city.parent_id AND state.type = 'STATE'
JOIN location country ON country.id = state.parent_id AND country.type = 'COUNTRY'
JOIN virus_strain vs ON vs.id = sp.strain_id
ORDER BY sp.hop_distance_from_target DESC;
