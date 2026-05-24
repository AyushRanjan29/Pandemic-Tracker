package com.pandemictracker.backend.dto;

import java.math.BigDecimal;

public record ResourceOptimizationResponse(
        Long cityId,
        String cityName,
        Long population,
        Long totalNewCases,
        Integer activeCases,
        Integer availableBeds,
        Integer availableIcuBeds,
        Integer availableVentilators,
        Integer availableOxygenCylinders,
        Integer availableVaccineDoses,
        BigDecimal casesPer100k,
        BigDecimal bedPressure,
        String riskStatus
) {
}
