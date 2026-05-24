package com.pandemictracker.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateInfectionLogRequest(
        @NotNull Long cityId,
        @NotNull Long strainId,
        OffsetDateTime observedAt,
        @NotNull @PositiveOrZero Integer newCases,
        @NotNull @PositiveOrZero Integer activeCases,
        @PositiveOrZero Integer recoveries,
        @PositiveOrZero Integer deaths,
        @PositiveOrZero Integer testCount,
        @Min(0) @Max(100) BigDecimal positivityRate,
        Long sourceInfectionLogId,
        @Min(0) @Max(100) BigDecimal sourceConfidence,
        String notes
) {
}
