package com.pandemictracker.frontend.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pandemictracker.frontend.dto.LocationOption;
import com.pandemictracker.frontend.dto.ResourceOptimizationView;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PandemicApiClient {

    private static final TypeReference<List<ResourceOptimizationView>> RESOURCE_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<LocationOption>> LOCATION_LIST_TYPE = new TypeReference<>() {
    };

    private final URI baseUri;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PandemicApiClient(String baseUrl) {
        this.baseUri = URI.create(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<List<ResourceOptimizationView>> fetchStateResourceOptimization(long stateId, int days) {
        URI uri = baseUri.resolve("resource-optimization/states/" + stateId + "?days=" + days);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::ensureSuccess)
                .thenApply(this::parseResourceOptimization);
    }

    public CompletableFuture<List<LocationOption>> fetchLocations(String type) {
        return fetchLocations(type, null);
    }

    public CompletableFuture<List<LocationOption>> fetchLocations(String type, Long parentId) {
        String path = "locations?type=" + type + (parentId == null ? "" : "&parentId=" + parentId);
        HttpRequest request = HttpRequest.newBuilder(baseUri.resolve(path))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::ensureSuccess)
                .thenApply(this::parseLocations);
    }

    public CompletableFuture<Void> createLocation(Map<String, Object> payload) {
        return post("locations", payload);
    }

    public CompletableFuture<Void> createInfectionLog(Map<String, Object> payload) {
        return post("manual-entry/infection-logs", payload);
    }

    public CompletableFuture<Void> createHospitalInventory(Map<String, Object> payload) {
        return post("manual-entry/hospital-inventories", payload);
    }

    public CompletableFuture<Void> createVaccineInventory(Map<String, Object> payload) {
        return post("manual-entry/vaccine-inventories", payload);
    }

    public CompletableFuture<Void> createCitySnapshot(Map<String, Object> payload) {
        return post("manual-entry/city-snapshots", payload);
    }

    public CompletableFuture<Void> updateCitySnapshot(long cityId, Map<String, Object> payload) {
        return sendJson("manual-entry/city-snapshots/" + cityId, "PUT", payload);
    }

    private CompletableFuture<Void> post(String path, Map<String, Object> payload) {
        return sendJson(path, "POST", payload);
    }

    private CompletableFuture<Void> sendJson(String path, String method, Map<String, Object> payload) {
        HttpRequest request = HttpRequest.newBuilder(baseUri.resolve(path))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(writeJson(payload)))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::ensureSuccess)
                .thenApply(body -> null);
    }

    private String ensureSuccess(HttpResponse<String> response) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        }
        throw new ApiClientException("API request failed with HTTP " + response.statusCode() + ": " + response.body());
    }

    private List<ResourceOptimizationView> parseResourceOptimization(String body) {
        try {
            return objectMapper.readValue(body, RESOURCE_LIST_TYPE);
        } catch (IOException exception) {
            throw new ApiClientException("Unable to parse resource optimization response", exception);
        }
    }

    private List<LocationOption> parseLocations(String body) {
        try {
            return objectMapper.readValue(body, LOCATION_LIST_TYPE);
        } catch (IOException exception) {
            throw new ApiClientException("Unable to parse locations response", exception);
        }
    }

    private String writeJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (IOException exception) {
            throw new ApiClientException("Unable to write request body", exception);
        }
    }
}
