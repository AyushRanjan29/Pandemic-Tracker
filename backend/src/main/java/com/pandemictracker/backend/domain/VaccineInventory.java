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

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "vaccine_inventory")
public class VaccineInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_location_id", nullable = false)
    private Location hospitalLocation;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String vaccineName;

    @Column(length = 120)
    private String manufacturer;

    @PositiveOrZero
    @Column(nullable = false)
    private Integer doseCount;

    @PositiveOrZero
    @Column(nullable = false)
    private Integer reservedDoseCount = 0;

    @NotNull
    @Column(nullable = false)
    private OffsetDateTime recordedAt = OffsetDateTime.now();

    private LocalDate expiresOn;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String updatedBy = "system";

    protected VaccineInventory() {
    }

    public VaccineInventory(Location hospitalLocation, String vaccineName, Integer doseCount) {
        this.hospitalLocation = hospitalLocation;
        this.vaccineName = vaccineName;
        this.doseCount = doseCount;
    }

    public Long getId() {
        return id;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setDoseCount(Integer doseCount) {
        this.doseCount = doseCount;
    }

    public void setReservedDoseCount(Integer reservedDoseCount) {
        this.reservedDoseCount = reservedDoseCount;
    }

    public void setRecordedAt(OffsetDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public void setExpiresOn(LocalDate expiresOn) {
        this.expiresOn = expiresOn;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
