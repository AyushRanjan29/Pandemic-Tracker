package com.pandemictracker.backend.repository;

import java.math.BigDecimal;

public interface ResourceOptimizationProjection {

    Long getCityId();

    String getCityName();

    Long getPopulation();

    Long getTotalNewCases();

    Integer getActiveCases();

    Integer getAvailableBeds();

    Integer getAvailableIcuBeds();

    Integer getAvailableVentilators();

    Integer getAvailableOxygenCylinders();

    Integer getAvailableVaccineDoses();

    BigDecimal getCasesPer100k();

    BigDecimal getBedPressure();

    String getRiskStatus();
}
