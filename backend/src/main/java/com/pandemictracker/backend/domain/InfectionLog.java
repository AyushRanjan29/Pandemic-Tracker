package com.pandemictracker.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "infection_log")
public class InfectionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "strain_id", nullable = false)
    private VirusStrain strain;

    @NotNull
    @Column(nullable = false)
    private OffsetDateTime observedAt;

    @PositiveOrZero
    @Column(nullable = false)
    private Integer newCases;

    @PositiveOrZero
    @Column(nullable = false)
    private Integer activeCases;

    @PositiveOrZero
    @Column(nullable = false)
    private Integer recoveries = 0;

    @PositiveOrZero
    @Column(nullable = false)
    private Integer deaths = 0;

    @PositiveOrZero
    private Integer testCount;

    @Min(0)
    @Max(100)
    @Column(precision = 5, scale = 2)
    private BigDecimal positivityRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_infection_log_id")
    private InfectionLog sourceInfectionLog;

    @Min(0)
    @Max(100)
    @Column(precision = 5, scale = 2)
    private BigDecimal sourceConfidence;

    private String notes;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected InfectionLog() {
    }

    public InfectionLog(
            Location location,
            VirusStrain strain,
            OffsetDateTime observedAt,
            Integer newCases,
            Integer activeCases
    ) {
        this.location = location;
        this.strain = strain;
        this.observedAt = observedAt;
        this.newCases = newCases;
        this.activeCases = activeCases;
    }

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setRecoveries(Integer recoveries) {
        this.recoveries = recoveries;
    }

    public void setNewCases(Integer newCases) {
        this.newCases = newCases;
    }

    public void setActiveCases(Integer activeCases) {
        this.activeCases = activeCases;
    }

    public void setObservedAt(OffsetDateTime observedAt) {
        this.observedAt = observedAt;
    }

    public void setDeaths(Integer deaths) {
        this.deaths = deaths;
    }

    public void setTestCount(Integer testCount) {
        this.testCount = testCount;
    }

    public void setPositivityRate(BigDecimal positivityRate) {
        this.positivityRate = positivityRate;
    }

    public void setSourceInfectionLog(InfectionLog sourceInfectionLog) {
        this.sourceInfectionLog = sourceInfectionLog;
    }

    public void setSourceConfidence(BigDecimal sourceConfidence) {
        this.sourceConfidence = sourceConfidence;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
