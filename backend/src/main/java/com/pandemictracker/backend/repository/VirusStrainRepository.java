package com.pandemictracker.backend.repository;

import com.pandemictracker.backend.domain.VirusStrain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VirusStrainRepository extends JpaRepository<VirusStrain, Long> {

    Optional<VirusStrain> findByPangoLineage(String pangoLineage);
}
