package com.pandemictracker.backend.repository;

import com.pandemictracker.backend.domain.HospitalInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HospitalInventoryRepository extends JpaRepository<HospitalInventory, Long> {

    Optional<HospitalInventory> findFirstByHospitalLocationIdOrderByRecordedAtDesc(Long hospitalLocationId);
}
