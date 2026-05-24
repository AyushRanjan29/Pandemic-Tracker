package com.pandemictracker.backend.dto;

import com.pandemictracker.backend.domain.LocationType;

public record LocationResponse(
        Long id,
        String name,
        LocationType type,
        Long parentId
) {
}
