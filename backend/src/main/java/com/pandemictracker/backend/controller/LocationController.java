package com.pandemictracker.backend.controller;

import com.pandemictracker.backend.domain.LocationType;
import com.pandemictracker.backend.dto.CreateLocationRequest;
import com.pandemictracker.backend.dto.LocationResponse;
import com.pandemictracker.backend.service.ManualEntryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

    private final ManualEntryService manualEntryService;

    public LocationController(ManualEntryService manualEntryService) {
        this.manualEntryService = manualEntryService;
    }

    @GetMapping
    public List<LocationResponse> findLocations(
            @RequestParam LocationType type,
            @RequestParam(required = false) Long parentId
    ) {
        return manualEntryService.findLocations(type, parentId);
    }

    @PostMapping
    public ResponseEntity<LocationResponse> createLocation(@Valid @RequestBody CreateLocationRequest request) {
        LocationResponse created = manualEntryService.createLocation(request);
        return ResponseEntity.created(URI.create("/api/v1/locations/" + created.id())).body(created);
    }
}
