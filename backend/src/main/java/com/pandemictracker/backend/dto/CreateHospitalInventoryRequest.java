package com.pandemictracker.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.OffsetDateTime;

public record CreateHospitalInventoryRequest(
        @NotNull Long hospitalLocationId,
        OffsetDateTime recordedAt,
        @NotNull @PositiveOrZero Integer totalBeds,
        @NotNull @PositiveOrZero Integer availableBeds,
        @PositiveOrZero Integer icuBeds,
        @PositiveOrZero Integer availableIcuBeds,
        @PositiveOrZero Integer ventilators,
        @PositiveOrZero Integer availableVentilators,
        @PositiveOrZero Integer oxygenCylinders,
        @PositiveOrZero Integer availableOxygenCylinders,
        String updatedBy
) {
}
