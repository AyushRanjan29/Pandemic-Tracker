package com.pandemictracker.backend.service;

import com.pandemictracker.backend.domain.HospitalInventory;
import com.pandemictracker.backend.domain.InfectionLog;
import com.pandemictracker.backend.domain.Location;
import com.pandemictracker.backend.domain.LocationType;
import com.pandemictracker.backend.domain.VaccineInventory;
import com.pandemictracker.backend.dto.CreateHospitalInventoryRequest;
import com.pandemictracker.backend.dto.CreateCitySnapshotRequest;
import com.pandemictracker.backend.dto.CreateInfectionLogRequest;
import com.pandemictracker.backend.dto.CreateLocationRequest;
import com.pandemictracker.backend.dto.CreateVaccineInventoryRequest;
import com.pandemictracker.backend.dto.LocationResponse;
import com.pandemictracker.backend.repository.HospitalInventoryRepository;
import com.pandemictracker.backend.repository.InfectionLogRepository;
import com.pandemictracker.backend.repository.LocationRepository;
import com.pandemictracker.backend.repository.VaccineInventoryRepository;
import com.pandemictracker.backend.repository.VirusStrainRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ManualEntryService {

    private static final long DEFAULT_CITY_POPULATION = 100_000L;

    private final LocationRepository locationRepository;
    private final VirusStrainRepository virusStrainRepository;
    private final InfectionLogRepository infectionLogRepository;
    private final HospitalInventoryRepository hospitalInventoryRepository;
    private final VaccineInventoryRepository vaccineInventoryRepository;
    private final JdbcTemplate jdbcTemplate;

    public ManualEntryService(
            LocationRepository locationRepository,
            VirusStrainRepository virusStrainRepository,
            InfectionLogRepository infectionLogRepository,
            HospitalInventoryRepository hospitalInventoryRepository,
            VaccineInventoryRepository vaccineInventoryRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.locationRepository = locationRepository;
        this.virusStrainRepository = virusStrainRepository;
        this.infectionLogRepository = infectionLogRepository;
        this.hospitalInventoryRepository = hospitalInventoryRepository;
        this.vaccineInventoryRepository = vaccineInventoryRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> findLocations(LocationType type, Long parentId) {
        List<Location> locations = parentId == null
                ? locationRepository.findByTypeOrderByNameAsc(type)
                : locationRepository.findByParentIdAndTypeOrderByNameAsc(parentId, type);

        return locations.stream().map(this::toLocationResponse).toList();
    }

    @Transactional
    public LocationResponse createLocation(CreateLocationRequest request) {
        Location location = new Location(request.name(), request.type());
        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());
        location.setPopulation(request.population());

        if (request.type() == LocationType.COUNTRY) {
            if (request.parentId() != null) {
                throw new IllegalArgumentException("Country locations must not have a parent");
            }
        } else {
            if (request.parentId() == null) {
                throw new IllegalArgumentException(request.type() + " locations require a parent");
            }
            Location parent = locationRepository.findById(request.parentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent location not found: " + request.parentId()));
            validateParentType(request.type(), parent.getType());
            location.setParent(parent);
        }

        return toLocationResponse(locationRepository.save(location));
    }

    @Transactional
    public Long createInfectionLog(CreateInfectionLogRequest request) {
        Location city = locationRepository.findByIdAndType(request.cityId(), LocationType.CITY)
                .orElseThrow(() -> new EntityNotFoundException("City location not found: " + request.cityId()));
        var strain = virusStrainRepository.findById(request.strainId())
                .orElseThrow(() -> new EntityNotFoundException("Virus strain not found: " + request.strainId()));

        InfectionLog log = new InfectionLog(
                city,
                strain,
                request.observedAt() == null ? OffsetDateTime.now() : request.observedAt(),
                request.newCases(),
                request.activeCases()
        );
        log.setRecoveries(defaultZero(request.recoveries()));
        log.setDeaths(defaultZero(request.deaths()));
        log.setTestCount(request.testCount());
        log.setPositivityRate(request.positivityRate());
        log.setSourceConfidence(request.sourceConfidence());
        log.setNotes(request.notes());

        if (request.sourceInfectionLogId() != null) {
            InfectionLog source = infectionLogRepository.findById(request.sourceInfectionLogId())
                    .orElseThrow(() -> new EntityNotFoundException("Source infection log not found: " + request.sourceInfectionLogId()));
            log.setSourceInfectionLog(source);
        }

        return infectionLogRepository.save(log).getId();
    }

    @Transactional
    public Long createHospitalInventory(CreateHospitalInventoryRequest request) {
        validateLessOrEqual("availableBeds", request.availableBeds(), "totalBeds", request.totalBeds());
        validateLessOrEqual("availableIcuBeds", defaultZero(request.availableIcuBeds()), "icuBeds", defaultZero(request.icuBeds()));
        validateLessOrEqual("availableVentilators", defaultZero(request.availableVentilators()), "ventilators", defaultZero(request.ventilators()));
        validateLessOrEqual("availableOxygenCylinders", defaultZero(request.availableOxygenCylinders()), "oxygenCylinders", defaultZero(request.oxygenCylinders()));

        Location hospital = locationRepository.findByIdAndType(request.hospitalLocationId(), LocationType.HOSPITAL)
                .orElseThrow(() -> new EntityNotFoundException("Hospital location not found: " + request.hospitalLocationId()));

        HospitalInventory inventory = new HospitalInventory(hospital, request.totalBeds(), request.availableBeds());
        inventory.setRecordedAt(request.recordedAt() == null ? OffsetDateTime.now() : request.recordedAt());
        inventory.setIcuBeds(defaultZero(request.icuBeds()));
        inventory.setAvailableIcuBeds(defaultZero(request.availableIcuBeds()));
        inventory.setVentilators(defaultZero(request.ventilators()));
        inventory.setAvailableVentilators(defaultZero(request.availableVentilators()));
        inventory.setOxygenCylinders(defaultZero(request.oxygenCylinders()));
        inventory.setAvailableOxygenCylinders(defaultZero(request.availableOxygenCylinders()));
        inventory.setUpdatedBy(defaultText(request.updatedBy(), "manual-entry"));

        return hospitalInventoryRepository.save(inventory).getId();
    }

    @Transactional
    public Long createVaccineInventory(CreateVaccineInventoryRequest request) {
        validateLessOrEqual("reservedDoseCount", defaultZero(request.reservedDoseCount()), "doseCount", request.doseCount());

        Location hospital = locationRepository.findByIdAndType(request.hospitalLocationId(), LocationType.HOSPITAL)
                .orElseThrow(() -> new EntityNotFoundException("Hospital location not found: " + request.hospitalLocationId()));

        VaccineInventory inventory = new VaccineInventory(hospital, request.vaccineName(), request.doseCount());
        inventory.setManufacturer(request.manufacturer());
        inventory.setReservedDoseCount(defaultZero(request.reservedDoseCount()));
        inventory.setRecordedAt(request.recordedAt() == null ? OffsetDateTime.now() : request.recordedAt());
        inventory.setExpiresOn(request.expiresOn());
        inventory.setUpdatedBy(defaultText(request.updatedBy(), "manual-entry"));

        return vaccineInventoryRepository.save(inventory).getId();
    }

    @Transactional
    public Long createCitySnapshot(CreateCitySnapshotRequest request) {
        Location state = resolveState(request);
        var strain = virusStrainRepository.findById(request.strainId() == null ? 1L : request.strainId())
                .orElseThrow(() -> new EntityNotFoundException("Virus strain not found: " + request.strainId()));

        Location city = resolveCity(request, state);

        Location hospital = resolveHospital(city);

        OffsetDateTime timestamp = request.observedAt() == null ? OffsetDateTime.now() : request.observedAt();

        InfectionLog infectionLog = new InfectionLog(
                city,
                strain,
                timestamp,
                request.newCases(),
                request.activeCases()
        );
        infectionLog.setRecoveries(0);
        infectionLog.setDeaths(0);
        infectionLogRepository.save(infectionLog);

        int beds = request.beds();
        int icu = defaultZero(request.icu());
        int ventilators = defaultZero(request.ventilators());
        int oxygen = defaultZero(request.oxygen());

        HospitalInventory hospitalInventory = new HospitalInventory(hospital, beds, beds);
        hospitalInventory.setRecordedAt(timestamp);
        hospitalInventory.setIcuBeds(icu);
        hospitalInventory.setAvailableIcuBeds(icu);
        hospitalInventory.setVentilators(ventilators);
        hospitalInventory.setAvailableVentilators(ventilators);
        hospitalInventory.setOxygenCylinders(oxygen);
        hospitalInventory.setAvailableOxygenCylinders(oxygen);
        hospitalInventory.setUpdatedBy("city-snapshot");
        hospitalInventoryRepository.save(hospitalInventory);

        if (defaultZero(request.vaccineDoses()) > 0) {
            VaccineInventory vaccineInventory = new VaccineInventory(
                    hospital,
                    defaultText(request.vaccineName(), "PanVax Booster"),
                    request.vaccineDoses()
            );
            vaccineInventory.setReservedDoseCount(0);
            vaccineInventory.setRecordedAt(timestamp);
            vaccineInventory.setUpdatedBy("city-snapshot");
            vaccineInventoryRepository.save(vaccineInventory);
        }

        return city.getId();
    }

    @Transactional
    public Long updateCitySnapshot(Long cityId, CreateCitySnapshotRequest request) {
        Location city = locationRepository.findByIdAndType(cityId, LocationType.CITY)
                .orElseThrow(() -> new EntityNotFoundException("City location not found: " + cityId));
        if (request.cityName() != null && !request.cityName().isBlank()) {
            city.setName(request.cityName().trim());
            locationRepository.save(city);
        }

        var strain = virusStrainRepository.findById(request.strainId() == null ? 1L : request.strainId())
                .orElseThrow(() -> new EntityNotFoundException("Virus strain not found: " + request.strainId()));
        OffsetDateTime timestamp = request.observedAt() == null ? OffsetDateTime.now() : request.observedAt();

        InfectionLog infectionLog = infectionLogRepository
                .findFirstByLocationIdAndStrainIdOrderByObservedAtDesc(city.getId(), strain.getId())
                .orElseGet(() -> new InfectionLog(city, strain, timestamp, request.newCases(), request.activeCases()));
        infectionLog.setObservedAt(timestamp);
        infectionLog.setNewCases(request.newCases());
        infectionLog.setActiveCases(request.activeCases());
        infectionLog.setRecoveries(0);
        infectionLog.setDeaths(0);
        infectionLogRepository.save(infectionLog);

        Location hospital = resolveHospital(city);
        int beds = request.beds();
        int icu = defaultZero(request.icu());
        int ventilators = defaultZero(request.ventilators());
        int oxygen = defaultZero(request.oxygen());

        HospitalInventory hospitalInventory = hospitalInventoryRepository
                .findFirstByHospitalLocationIdOrderByRecordedAtDesc(hospital.getId())
                .orElseGet(() -> new HospitalInventory(hospital, beds, beds));
        hospitalInventory.setRecordedAt(timestamp);
        hospitalInventory.setTotalBeds(beds);
        hospitalInventory.setAvailableBeds(beds);
        hospitalInventory.setIcuBeds(icu);
        hospitalInventory.setAvailableIcuBeds(icu);
        hospitalInventory.setVentilators(ventilators);
        hospitalInventory.setAvailableVentilators(ventilators);
        hospitalInventory.setOxygenCylinders(oxygen);
        hospitalInventory.setAvailableOxygenCylinders(oxygen);
        hospitalInventory.setUpdatedBy("city-edit");
        hospitalInventoryRepository.save(hospitalInventory);

        String vaccineName = defaultText(request.vaccineName(), "PanVax Booster");
        int vaccineDoses = defaultZero(request.vaccineDoses());
        VaccineInventory vaccineInventory = vaccineInventoryRepository
                .findFirstByHospitalLocationIdAndVaccineNameOrderByRecordedAtDesc(hospital.getId(), vaccineName)
                .orElseGet(() -> new VaccineInventory(hospital, vaccineName, vaccineDoses));
        vaccineInventory.setDoseCount(vaccineDoses);
        vaccineInventory.setReservedDoseCount(0);
        vaccineInventory.setRecordedAt(timestamp);
        vaccineInventory.setUpdatedBy("city-edit");
        vaccineInventoryRepository.save(vaccineInventory);

        return city.getId();
    }

    private Location resolveState(CreateCitySnapshotRequest request) {
        if (request.stateId() != null) {
            return locationRepository.findByIdAndType(request.stateId(), LocationType.STATE)
                    .orElseThrow(() -> new EntityNotFoundException("State location not found: " + request.stateId()));
        }
        if (request.stateName() == null || request.stateName().isBlank()) {
            throw new IllegalArgumentException("Select a state or enter a new state name");
        }
        Location country = locationRepository.findByTypeOrderByNameAsc(LocationType.COUNTRY)
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No country location found"));
        var existingState = locationRepository.findByParentIdAndTypeAndNameIgnoreCase(
                country.getId(),
                LocationType.STATE,
                request.stateName().trim()
        );
        if (existingState.isPresent()) {
            return existingState.get();
        }
        Location state = new Location(request.stateName().trim(), LocationType.STATE);
        state.setParent(country);
        return locationRepository.save(state);
    }

    private Location resolveCity(CreateCitySnapshotRequest request, Location state) {
        if (request.cityId() != null) {
            var existingCity = locationRepository.findByIdAndType(request.cityId(), LocationType.CITY);
            if (existingCity.isPresent()) {
                Location city = existingCity.get();
                if (city.getParent() == null || !city.getParent().getId().equals(state.getId())) {
                    throw new IllegalArgumentException("City ID already belongs to another state");
                }
                city.setName(request.cityName().trim());
                return locationRepository.save(city);
            }
            return createCityWithManualId(request.cityId(), request.cityName().trim(), state);
        }

        String cityName = request.cityName().trim();
        var existingByName = locationRepository.findByParentIdAndTypeAndNameIgnoreCase(
                state.getId(),
                LocationType.CITY,
                cityName
        );
        if (existingByName.isPresent()) {
            Location city = existingByName.get();
            city.setName(cityName);
            return locationRepository.save(city);
        }

        Location city = new Location(cityName, LocationType.CITY);
        city.setParent(state);
        city.setPopulation(DEFAULT_CITY_POPULATION);
        return locationRepository.save(city);
    }

    private Location createCityWithManualId(Long cityId, String cityName, Location state) {
        jdbcTemplate.update("""
                        INSERT INTO location (id, name, type, parent_id, population)
                        VALUES (?, ?, 'CITY', ?, ?)
                        """,
                cityId,
                cityName,
                state.getId(),
                DEFAULT_CITY_POPULATION
        );
        return locationRepository.findByIdAndType(cityId, LocationType.CITY)
                .orElseThrow(() -> new EntityNotFoundException("City location not found after create: " + cityId));
    }

    private Location resolveHospital(Location city) {
        return locationRepository.findByParentIdAndTypeOrderByNameAsc(city.getId(), LocationType.HOSPITAL)
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    Location hospital = new Location(city.getName() + " Public Health Center", LocationType.HOSPITAL);
                    hospital.setParent(city);
                    return locationRepository.save(hospital);
                });
    }

    private LocationResponse toLocationResponse(Location location) {
        Long parentId = location.getParent() == null ? null : location.getParent().getId();
        return new LocationResponse(location.getId(), location.getName(), location.getType(), parentId);
    }

    private void validateParentType(LocationType childType, LocationType parentType) {
        boolean valid = switch (childType) {
            case STATE -> parentType == LocationType.COUNTRY;
            case CITY -> parentType == LocationType.STATE;
            case HOSPITAL -> parentType == LocationType.CITY;
            case COUNTRY -> parentType == null;
        };
        if (!valid) {
            throw new IllegalArgumentException(childType + " cannot be created under " + parentType);
        }
    }

    private void validateLessOrEqual(String childName, int childValue, String parentName, int parentValue) {
        if (childValue > parentValue) {
            throw new IllegalArgumentException(childName + " cannot exceed " + parentName);
        }
    }

    private Integer defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
