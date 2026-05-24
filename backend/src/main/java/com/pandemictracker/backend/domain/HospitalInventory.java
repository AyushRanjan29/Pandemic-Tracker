package com.pandemictracker.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.OffsetDateTime;

@Entity
@Table(name = "hospital_inventory")
public class HospitalInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_location_id", nullable = false)
    private Location hospitalLocation;

    @NotNull
    @Column(nullable = false)
    private OffsetDateTime recordedAt = OffsetDateTime.now();

    @PositiveOrZero
    @Column(nullable = false)
    private Integer totalBeds;

    @PositiveOrZero
    @Column(nullable = false)
    private Integer availableBeds;

    @PositiveOrZero
    @Column(nullable = false)
    private Integer icuBeds = 0;

    @PositiveOrZero
    @Column(nullable = false)
    private Integer availableIcuBeds = 0;

    @PositiveOrZero
    @Column(nullable = false)
    private Integer ventilators = 0;

    @PositiveOrZero
    @Column(nullable = false)
    private Integer availableVentilators = 0;

    @PositiveOrZero
    @Column(nullable = false)
    private Integer oxygenCylinders = 0;

    @PositiveOrZero
    @Column(nullable = false)
    private Integer availableOxygenCylinders = 0;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String updatedBy = "system";

    protected HospitalInventory() {
    }

    public HospitalInventory(Location hospitalLocation, Integer totalBeds, Integer availableBeds) {
        this.hospitalLocation = hospitalLocation;
        this.totalBeds = totalBeds;
        this.availableBeds = availableBeds;
    }

    public Long getId() {
        return id;
    }

    public void setRecordedAt(OffsetDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public void setTotalBeds(Integer totalBeds) {
        this.totalBeds = totalBeds;
    }

    public void setAvailableBeds(Integer availableBeds) {
        this.availableBeds = availableBeds;
    }

    public void setIcuBeds(Integer icuBeds) {
        this.icuBeds = icuBeds;
    }

    public void setAvailableIcuBeds(Integer availableIcuBeds) {
        this.availableIcuBeds = availableIcuBeds;
    }

    public void setVentilators(Integer ventilators) {
        this.ventilators = ventilators;
    }

    public void setAvailableVentilators(Integer availableVentilators) {
        this.availableVentilators = availableVentilators;
    }

    public void setOxygenCylinders(Integer oxygenCylinders) {
        this.oxygenCylinders = oxygenCylinders;
    }

    public void setAvailableOxygenCylinders(Integer availableOxygenCylinders) {
        this.availableOxygenCylinders = availableOxygenCylinders;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
