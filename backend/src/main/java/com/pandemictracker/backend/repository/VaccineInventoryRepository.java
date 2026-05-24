package com.pandemictracker.backend.repository;

import com.pandemictracker.backend.domain.VaccineInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VaccineInventoryRepository extends JpaRepository<VaccineInventory, Long> {

    Optional<VaccineInventory> findFirstByHospitalLocationIdAndVaccineNameOrderByRecordedAtDesc(
            Long hospitalLocationId,
            String vaccineName
    );
}
