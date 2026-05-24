package com.pandemictracker.backend.repository;

import com.pandemictracker.backend.domain.Location;
import com.pandemictracker.backend.domain.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByIdAndType(Long id, LocationType type);

    List<Location> findByTypeOrderByNameAsc(LocationType type);

    List<Location> findByParentIdAndTypeOrderByNameAsc(Long parentId, LocationType type);

    Optional<Location> findByParentIdAndTypeAndNameIgnoreCase(Long parentId, LocationType type, String name);

    @Query(value = """
            WITH RECURSIVE state_tree AS (
                SELECT id, name, type, parent_id, population
                FROM location
                WHERE id = :stateId
                  AND type = 'STATE'

                UNION ALL

                SELECT child.id, child.name, child.type, child.parent_id, child.population
                FROM location child
                JOIN state_tree parent ON child.parent_id = parent.id
            ),
            cities AS (
                SELECT id, name, population
                FROM state_tree
                WHERE type = 'CITY'
            ),
            infection_window AS (
                SELECT
                    il.location_id AS city_id,
                    SUM(il.new_cases)::bigint AS total_new_cases
                FROM infection_log il
                JOIN cities c ON c.id = il.location_id
                WHERE il.observed_at >= now() - (:days * interval '1 day')
                GROUP BY il.location_id
            ),
            latest_active_cases AS (
                SELECT DISTINCT ON (il.location_id)
                    il.location_id AS city_id,
                    il.active_cases
                FROM infection_log il
                JOIN cities c ON c.id = il.location_id
                ORDER BY il.location_id, il.observed_at DESC
            ),
            latest_hospital_inventory AS (
                SELECT DISTINCT ON (hi.hospital_location_id)
                    hi.hospital_location_id,
                    hi.available_beds,
                    hi.available_icu_beds,
                    hi.available_ventilators,
                    hi.available_oxygen_cylinders
                FROM hospital_inventory hi
                JOIN state_tree hospital ON hospital.id = hi.hospital_location_id
                WHERE hospital.type = 'HOSPITAL'
                ORDER BY hi.hospital_location_id, hi.recorded_at DESC
            ),
            hospital_summary AS (
                SELECT
                    hospital.parent_id AS city_id,
                    COALESCE(SUM(lhi.available_beds), 0)::int AS available_beds,
                    COALESCE(SUM(lhi.available_icu_beds), 0)::int AS available_icu_beds,
                    COALESCE(SUM(lhi.available_ventilators), 0)::int AS available_ventilators,
                    COALESCE(SUM(lhi.available_oxygen_cylinders), 0)::int AS available_oxygen_cylinders
                FROM state_tree hospital
                JOIN latest_hospital_inventory lhi ON lhi.hospital_location_id = hospital.id
                WHERE hospital.type = 'HOSPITAL'
                GROUP BY hospital.parent_id
            ),
            latest_vaccine_inventory AS (
                SELECT DISTINCT ON (vi.hospital_location_id, vi.vaccine_name)
                    vi.hospital_location_id,
                    vi.vaccine_name,
                    GREATEST(vi.dose_count - vi.reserved_dose_count, 0) AS available_doses
                FROM vaccine_inventory vi
                JOIN state_tree hospital ON hospital.id = vi.hospital_location_id
                WHERE hospital.type = 'HOSPITAL'
                  AND (vi.expires_on IS NULL OR vi.expires_on >= current_date)
                ORDER BY vi.hospital_location_id, vi.vaccine_name, vi.recorded_at DESC
            ),
            vaccine_summary AS (
                SELECT
                    hospital.parent_id AS city_id,
                    COALESCE(SUM(lvi.available_doses), 0)::int AS available_vaccine_doses
                FROM state_tree hospital
                JOIN latest_vaccine_inventory lvi ON lvi.hospital_location_id = hospital.id
                WHERE hospital.type = 'HOSPITAL'
                GROUP BY hospital.parent_id
            ),
            city_rollup AS (
                SELECT
                    c.id AS city_id,
                    c.name AS city_name,
                    c.population,
                    COALESCE(iw.total_new_cases, 0) AS total_new_cases,
                    COALESCE(lac.active_cases, 0) AS active_cases,
                    COALESCE(hs.available_beds, 0) AS available_beds,
                    COALESCE(hs.available_icu_beds, 0) AS available_icu_beds,
                    COALESCE(hs.available_ventilators, 0) AS available_ventilators,
                    COALESCE(hs.available_oxygen_cylinders, 0) AS available_oxygen_cylinders,
                    COALESCE(vs.available_vaccine_doses, 0) AS available_vaccine_doses,
                    COALESCE(ROUND((COALESCE(iw.total_new_cases, 0)::numeric / NULLIF(c.population, 0)) * 100000, 2), 0) AS cases_per_100k,
                    CASE
                        WHEN COALESCE(hs.available_beds, 0) = 0 AND COALESCE(lac.active_cases, 0) > 0 THEN 999.99
                        ELSE COALESCE(ROUND(COALESCE(lac.active_cases, 0)::numeric / NULLIF(hs.available_beds, 0), 2), 0)
                    END AS bed_pressure
                FROM cities c
                LEFT JOIN infection_window iw ON iw.city_id = c.id
                LEFT JOIN latest_active_cases lac ON lac.city_id = c.id
                LEFT JOIN hospital_summary hs ON hs.city_id = c.id
                LEFT JOIN vaccine_summary vs ON vs.city_id = c.id
            )
            SELECT
                city_id AS "cityId",
                city_name AS "cityName",
                population AS "population",
                total_new_cases AS "totalNewCases",
                active_cases AS "activeCases",
                available_beds AS "availableBeds",
                available_icu_beds AS "availableIcuBeds",
                available_ventilators AS "availableVentilators",
                available_oxygen_cylinders AS "availableOxygenCylinders",
                available_vaccine_doses AS "availableVaccineDoses",
                cases_per_100k AS "casesPer100k",
                bed_pressure AS "bedPressure",
                CASE
                    WHEN cases_per_100k >= 100 OR bed_pressure >= 3 OR available_vaccine_doses < active_cases THEN 'CRITICAL'
                    WHEN cases_per_100k >= 50 OR bed_pressure >= 2 OR available_vaccine_doses < active_cases * 2 THEN 'HIGH'
                    WHEN cases_per_100k >= 20 OR bed_pressure >= 1 THEN 'ELEVATED'
                    ELSE 'STABLE'
                END AS "riskStatus"
            FROM city_rollup
            ORDER BY
                CASE
                    WHEN cases_per_100k >= 100 OR bed_pressure >= 3 OR available_vaccine_doses < active_cases THEN 4
                    WHEN cases_per_100k >= 50 OR bed_pressure >= 2 OR available_vaccine_doses < active_cases * 2 THEN 3
                    WHEN cases_per_100k >= 20 OR bed_pressure >= 1 THEN 2
                    ELSE 1
                END DESC,
                cases_per_100k DESC,
                city_name
            """, nativeQuery = true)
    List<ResourceOptimizationProjection> findResourceOptimizationByState(
            @Param("stateId") Long stateId,
            @Param("days") int days
    );
}
