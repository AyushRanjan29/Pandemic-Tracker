package com.pandemictracker.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record CreateVaccineInventoryRequest(
        @NotNull Long hospitalLocationId,
        @NotBlank String vaccineName,
        String manufacturer,
        @NotNull @PositiveOrZero Integer doseCount,
        @PositiveOrZero Integer reservedDoseCount,
        OffsetDateTime recordedAt,
        LocalDate expiresOn,
        String updatedBy
) {
}
