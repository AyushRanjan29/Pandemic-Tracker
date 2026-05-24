package com.pandemictracker.backend.repository;

import com.pandemictracker.backend.domain.InfectionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InfectionLogRepository extends JpaRepository<InfectionLog, Long> {

    Optional<InfectionLog> findFirstByLocationIdAndStrainIdOrderByObservedAtDesc(Long locationId, Long strainId);
}
