package com.pandemictracker.backend.dto;

import com.pandemictracker.backend.domain.LocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateLocationRequest(
        @NotBlank String name,
        @NotNull LocationType type,
        Long parentId,
        BigDecimal latitude,
        BigDecimal longitude,
        @PositiveOrZero Long population
) {
}
