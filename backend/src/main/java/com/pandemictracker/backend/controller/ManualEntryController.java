package com.pandemictracker.backend.controller;

import com.pandemictracker.backend.dto.CreateHospitalInventoryRequest;
import com.pandemictracker.backend.dto.CreateCitySnapshotRequest;
import com.pandemictracker.backend.dto.CreateInfectionLogRequest;
import com.pandemictracker.backend.dto.CreateVaccineInventoryRequest;
import com.pandemictracker.backend.service.ManualEntryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/manual-entry")
public class ManualEntryController {

    private final ManualEntryService manualEntryService;

    public ManualEntryController(ManualEntryService manualEntryService) {
        this.manualEntryService = manualEntryService;
    }

    @PostMapping("/infection-logs")
    public ResponseEntity<Map<String, Long>> createInfectionLog(@Valid @RequestBody CreateInfectionLogRequest request) {
        Long id = manualEntryService.createInfectionLog(request);
        return ResponseEntity.created(URI.create("/api/v1/manual-entry/infection-logs/" + id)).body(Map.of("id", id));
    }

    @PostMapping("/hospital-inventories")
    public ResponseEntity<Map<String, Long>> createHospitalInventory(@Valid @RequestBody CreateHospitalInventoryRequest request) {
        Long id = manualEntryService.createHospitalInventory(request);
        return ResponseEntity.created(URI.create("/api/v1/manual-entry/hospital-inventories/" + id)).body(Map.of("id", id));
    }

    @PostMapping("/vaccine-inventories")
    public ResponseEntity<Map<String, Long>> createVaccineInventory(@Valid @RequestBody CreateVaccineInventoryRequest request) {
        Long id = manualEntryService.createVaccineInventory(request);
        return ResponseEntity.created(URI.create("/api/v1/manual-entry/vaccine-inventories/" + id)).body(Map.of("id", id));
    }

    @PostMapping("/city-snapshots")
    public ResponseEntity<Map<String, Long>> createCitySnapshot(@Valid @RequestBody CreateCitySnapshotRequest request) {
        Long id = manualEntryService.createCitySnapshot(request);
        return ResponseEntity.created(URI.create("/api/v1/manual-entry/city-snapshots/" + id)).body(Map.of("cityId", id));
    }

    @PutMapping("/city-snapshots/{cityId}")
    public ResponseEntity<Map<String, Long>> updateCitySnapshot(
            @PathVariable Long cityId,
            @Valid @RequestBody CreateCitySnapshotRequest request
    ) {
        Long id = manualEntryService.updateCitySnapshot(cityId, request);
        return ResponseEntity.ok(Map.of("cityId", id));
    }
}
