package com.pandemictracker.frontend.dto;

public record LocationOption(
        Long id,
        String name,
        String type,
        Long parentId
) {
}
