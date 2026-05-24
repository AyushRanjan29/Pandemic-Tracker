package com.pandemictracker.backend.service;

import com.pandemictracker.backend.domain.LocationType;
import com.pandemictracker.backend.dto.ResourceOptimizationResponse;
import com.pandemictracker.backend.repository.LocationRepository;
import com.pandemictracker.backend.repository.ResourceOptimizationProjection;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResourceOptimizationService {

    private static final int DEFAULT_DAYS = 14;
    private static final int MAX_DAYS = 365;

    private final LocationRepository locationRepository;

    public ResourceOptimizationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Transactional(readOnly = true)
    public List<ResourceOptimizationResponse> getStateResourceOptimization(Long stateId, Integer days) {
        int windowDays = normalizeWindow(days);

        locationRepository.findByIdAndType(stateId, LocationType.STATE)
                .orElseThrow(() -> new EntityNotFoundException("State location not found: " + stateId));

        return locationRepository.findResourceOptimizationByState(stateId, windowDays)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private int normalizeWindow(Integer days) {
        if (days == null) {
            return DEFAULT_DAYS;
        }
        if (days < 1 || days > MAX_DAYS) {
            throw new IllegalArgumentException("days must be between 1 and " + MAX_DAYS);
        }
        return days;
    }

    private ResourceOptimizationResponse toResponse(ResourceOptimizationProjection row) {
        return new ResourceOptimizationResponse(
                row.getCityId(),
                row.getCityName(),
                row.getPopulation(),
                row.getTotalNewCases(),
                row.getActiveCases(),
                row.getAvailableBeds(),
                row.getAvailableIcuBeds(),
                row.getAvailableVentilators(),
                row.getAvailableOxygenCylinders(),
                row.getAvailableVaccineDoses(),
                row.getCasesPer100k(),
                row.getBedPressure(),
                row.getRiskStatus()
        );
    }
}
