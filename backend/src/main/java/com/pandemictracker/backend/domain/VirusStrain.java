package com.pandemictracker.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "virus_strain")
public class VirusStrain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 80)
    private String whoLabel;

    @NotBlank
    @Column(nullable = false, unique = true, length = 80)
    private String pangoLineage;

    @Column(length = 120)
    private String commonName;

    private LocalDate firstDetectedOn;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel riskLevel = RiskLevel.MONITORING;

    @Positive
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal transmissibilityFactor = BigDecimal.ONE;

    @PositiveOrZero
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal immuneEscapeScore = BigDecimal.ZERO;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected VirusStrain() {
    }

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }
}
