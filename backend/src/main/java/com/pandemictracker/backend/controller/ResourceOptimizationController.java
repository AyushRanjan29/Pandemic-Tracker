package com.pandemictracker.backend.controller;

import com.pandemictracker.backend.dto.ResourceOptimizationResponse;
import com.pandemictracker.backend.service.ResourceOptimizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resource-optimization")
public class ResourceOptimizationController {

    private final ResourceOptimizationService resourceOptimizationService;

    public ResourceOptimizationController(ResourceOptimizationService resourceOptimizationService) {
        this.resourceOptimizationService = resourceOptimizationService;
    }

    @GetMapping("/states/{stateId}")
    public ResponseEntity<List<ResourceOptimizationResponse>> getStateResourceOptimization(
            @PathVariable Long stateId,
            @RequestParam(required = false) Integer days
    ) {
        return ResponseEntity.ok(resourceOptimizationService.getStateResourceOptimization(stateId, days));
    }
}
