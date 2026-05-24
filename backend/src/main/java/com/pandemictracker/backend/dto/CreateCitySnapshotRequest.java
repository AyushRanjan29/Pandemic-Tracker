package com.pandemictracker.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.OffsetDateTime;

public record CreateCitySnapshotRequest(
        Long stateId,
        String stateName,
        Long cityId,
        @NotBlank String cityName,
        Long strainId,
        OffsetDateTime observedAt,
        @NotNull @PositiveOrZero Integer newCases,
        @NotNull @PositiveOrZero Integer activeCases,
        @NotNull @PositiveOrZero Integer beds,
        @PositiveOrZero Integer icu,
        @PositiveOrZero Integer ventilators,
        @PositiveOrZero Integer oxygen,
        @PositiveOrZero Integer vaccineDoses,
        String vaccineName
) {
}
