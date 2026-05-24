package com.pandemictracker.frontend;

import com.pandemictracker.frontend.api.PandemicApiClient;
import com.pandemictracker.frontend.dto.LocationOption;
import com.pandemictracker.frontend.dto.ResourceOptimizationView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PandemicTrackerClientApp extends Application {

    private final TableView<ResourceOptimizationView> tableView = new TableView<>();
    private final ComboBox<LocationOption> stateComboBox = new ComboBox<>();
    private final Button refreshButton = new Button("Refresh");
    private final Button addCityButton = new Button("Add City Data");
    private final Button editCityButton = new Button("Edit City Data");
    private final ProgressIndicator progressIndicator = new ProgressIndicator();
    private final Label statusLabel = new Label("Ready");
    private final Label totalCitiesLabel = metricValue("0");
    private final Label activeCasesLabel = metricValue("0");
    private final Label newCasesLabel = metricValue("0");
    private final Label bedsLabel = metricValue("0");
    private final Label vaccineLabel = metricValue("0");
    private final Label criticalLabel = metricValue("0");

    private PandemicApiClient apiClient;

    @Override
    public void start(Stage stage) {
        String apiBaseUrl = System.getenv().getOrDefault("PANDEMIC_API_BASE_URL", "http://localhost:8080/api/v1");
        apiClient = new PandemicApiClient(apiBaseUrl);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(buildHeader());
        root.setCenter(buildDashboard());
        root.setBottom(buildStatusBar());

        Scene scene = new Scene(root, 1240, 760);
        scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        stage.setTitle("Pandemic Tracker Dashboard");
        stage.setScene(scene);
        stage.setMinWidth(1060);
        stage.setMinHeight(680);
        stage.show();

        loadStates();
    }

    private VBox buildHeader() {
        Label title = new Label("Pandemic Operations Dashboard");
        title.getStyleClass().add("app-title");
        Label subtitle = new Label("Regional infection pressure, medical capacity, and vaccine readiness");
        subtitle.getStyleClass().add("app-subtitle");

        stateComboBox.setPrefWidth(240);
        stateComboBox.setConverter(locationConverter());

        progressIndicator.setMaxSize(22, 22);
        progressIndicator.setVisible(false);

        refreshButton.setDefaultButton(true);
        refreshButton.setOnAction(event -> refreshData());
        addCityButton.getStyleClass().add("secondary-button");
        addCityButton.setOnAction(event -> showAddCityDialog());
        editCityButton.getStyleClass().add("secondary-button");
        editCityButton.setOnAction(event -> showEditCityDialog());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox toolbar = new HBox(
                12,
                new Label("State"),
                stateComboBox,
                refreshButton,
                addCityButton,
                editCityButton,
                spacer,
                progressIndicator
        );
        toolbar.getStyleClass().add("toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(12, new VBox(2, title, subtitle), toolbar);
        header.getStyleClass().add("header");
        header.setPadding(new Insets(22, 24, 16, 24));
        return header;
    }

    private VBox buildDashboard() {
        HBox metrics = new HBox(
                14,
                metricCard("Cities", totalCitiesLabel),
                metricCard("Active Cases", activeCasesLabel),
                metricCard("New Cases", newCasesLabel),
                metricCard("Open Beds", bedsLabel),
                metricCard("Vaccine Doses", vaccineLabel),
                metricCard("Critical Cities", criticalLabel)
        );
        metrics.getStyleClass().add("metric-strip");

        VBox dashboard = new VBox(16, metrics, buildTable());
        dashboard.setPadding(new Insets(18, 24, 18, 24));
        VBox.setVgrow(tableView, Priority.ALWAYS);
        return dashboard;
    }

    private VBox metricCard(String title, Label value) {
        Label label = new Label(title);
        label.getStyleClass().add("metric-label");
        VBox card = new VBox(6, label, value);
        card.getStyleClass().add("metric-card");
        card.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private Label metricValue(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("metric-value");
        return label;
    }

    private TableView<ResourceOptimizationView> buildTable() {
        tableView.getStyleClass().add("dashboard-table");
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableView.setPlaceholder(new Label("No resource data available"));

        TableColumn<ResourceOptimizationView, Number> cityId = numberColumn("City ID", row -> row.cityId());
        TableColumn<ResourceOptimizationView, String> city = textColumn("City", row -> row.cityName());
        TableColumn<ResourceOptimizationView, String> risk = textColumn("Risk", row -> row.riskStatus());
        TableColumn<ResourceOptimizationView, Number> active = numberColumn("Active", row -> row.activeCases());
        TableColumn<ResourceOptimizationView, Number> newCases = numberColumn("New Cases", row -> row.totalNewCases());
        TableColumn<ResourceOptimizationView, BigDecimal> per100k = decimalColumn("Cases / 100k", row -> row.casesPer100k());
        TableColumn<ResourceOptimizationView, Number> beds = numberColumn("Beds", row -> row.availableBeds());
        TableColumn<ResourceOptimizationView, Number> icu = numberColumn("ICU", row -> row.availableIcuBeds());
        TableColumn<ResourceOptimizationView, Number> ventilators = numberColumn("Ventilators", row -> row.availableVentilators());
        TableColumn<ResourceOptimizationView, Number> oxygen = numberColumn("Oxygen", row -> row.availableOxygenCylinders());
        TableColumn<ResourceOptimizationView, Number> vaccines = numberColumn("Vaccine Doses", row -> row.availableVaccineDoses());
        TableColumn<ResourceOptimizationView, BigDecimal> pressure = decimalColumn("Bed Pressure", row -> row.bedPressure());

        tableView.getColumns().addAll(cityId, city, risk, active, newCases, per100k, beds, icu, ventilators, oxygen, vaccines, pressure);
        tableView.setRowFactory(view -> {
            TableRow<ResourceOptimizationView> row = new TableRow<>();
            row.itemProperty().addListener((observable, previous, current) -> {
                row.getStyleClass().removeAll("risk-critical", "risk-high", "risk-elevated", "risk-stable");
                if (current != null) {
                    row.getStyleClass().add("risk-" + current.riskStatus().toLowerCase());
                }
            });
            return row;
        });
        return tableView;
    }

    private HBox buildStatusBar() {
        HBox statusBar = new HBox(statusLabel);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(9, 24, 10, 24));
        return statusBar;
    }

    private void loadStates() {
        setLoading(true, "Loading states...");
        apiClient.fetchLocations("STATE")
                .whenComplete((states, throwable) -> Platform.runLater(() -> {
                    setLoading(false, "Ready");
                    if (throwable != null) {
                        statusLabel.setText(rootMessage(throwable));
                        return;
                    }
                    Long previousId = stateComboBox.getValue() == null ? 2L : stateComboBox.getValue().id();
                    stateComboBox.setItems(FXCollections.observableArrayList(states));
                    states.stream()
                            .filter(state -> state.id().equals(previousId))
                            .findFirst()
                            .or(() -> states.stream().findFirst())
                            .ifPresent(stateComboBox::setValue);
                    refreshData();
                }));
    }

    private void refreshData() {
        LocationOption selectedState = stateComboBox.getValue();
        if (selectedState == null) {
            statusLabel.setText("Select a state");
            return;
        }

        int days = 14;
        setLoading(true, "Loading " + selectedState.name() + "...");

        apiClient.fetchStateResourceOptimization(selectedState.id(), days)
                .whenComplete((rows, throwable) -> Platform.runLater(() -> {
                    setLoading(false, "Ready");
                    if (throwable != null) {
                        statusLabel.setText(rootMessage(throwable));
                        return;
                    }
                    applyRows(rows);
                }));
    }

    private void showAddCityDialog() {
        Optional<Boolean> createNewState = askStateMode();
        if (createNewState.isEmpty()) {
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add City Data");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setContent(buildAddCityForm(createNewState.get()));
        dialog.getDialogPane().setPrefWidth(680);
        dialog.showAndWait();
    }

    private void showEditCityDialog() {
        ResourceOptimizationView selectedCity = tableView.getSelectionModel().getSelectedItem();
        if (selectedCity == null) {
            statusLabel.setText("Select a city row to edit");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit City Data");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setContent(buildEditCityForm(selectedCity));
        dialog.getDialogPane().setPrefWidth(680);
        dialog.showAndWait();
    }

    private Optional<Boolean> askStateMode() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("State Entry");

        ButtonType continueButton = new ButtonType("Continue", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(continueButton, ButtonType.CANCEL);

        ToggleGroup group = new ToggleGroup();
        RadioButton existing = new RadioButton("Use existing state");
        RadioButton create = new RadioButton("Add new state");
        existing.setToggleGroup(group);
        create.setToggleGroup(group);
        existing.setSelected(true);

        VBox content = new VBox(12, new Label("Choose how you want to add the city data"), existing, create);
        content.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(button -> button == continueButton ? create.isSelected() : null);
        return dialog.showAndWait();
    }

    private GridPane buildAddCityForm(boolean createNewState) {
        ComboBox<LocationOption> state = new ComboBox<>();
        state.setConverter(locationConverter());
        state.setItems(stateComboBox.getItems());
        state.setValue(stateComboBox.getValue());
        state.setPrefWidth(280);

        TextField newState = new TextField();
        newState.setPromptText("State name");
        TextField cityId = new TextField();
        TextField cityName = new TextField();
        TextField newCases = new TextField();
        TextField activeCases = new TextField();
        TextField beds = new TextField();
        TextField icu = new TextField();
        TextField ventilators = new TextField();
        TextField oxygen = new TextField();
        TextField vaccineDoses = new TextField();
        Label feedback = new Label();
        Button save = new Button("Save City Data");

        save.setOnAction(event -> runManualSave(feedback, () -> {
            Map<String, Object> payload = new LinkedHashMap<>();
            if (createNewState) {
                payload.put("stateName", requiredText(newState, "New state"));
            } else {
                payload.put("stateId", requiredLocationId(state, "State"));
            }
            putOptionalLong(payload, "cityId", cityId);
            payload.put("cityName", requiredText(cityName, "City name"));
            payload.put("newCases", requiredInteger(newCases, "New cases"));
            payload.put("activeCases", requiredInteger(activeCases, "Active cases"));
            payload.put("beds", requiredInteger(beds, "Beds"));
            putOptionalInteger(payload, "icu", icu);
            putOptionalInteger(payload, "ventilators", ventilators);
            putOptionalInteger(payload, "oxygen", oxygen);
            putOptionalInteger(payload, "vaccineDoses", vaccineDoses);
            payload.put("vaccineName", "PanVax Booster");
            return apiClient.createCitySnapshot(payload);
        }));

        GridPane grid = new GridPane();
        grid.getStyleClass().add("entry-grid");
        grid.setHgap(14);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        if (createNewState) {
            addWideRow(grid, 0, "New state", newState);
        } else {
            addWideRow(grid, 0, "Select state", state);
        }
        addRow(grid, 1, "City ID", cityId, "City name", cityName);
        addRow(grid, 2, "Active cases", activeCases, "New cases", newCases);
        addRow(grid, 3, "Beds", beds, "ICU", icu);
        addRow(grid, 4, "Ventilators", ventilators, "Oxygen", oxygen);
        addRow(grid, 5, "Vaccine dose", vaccineDoses, "", new Label(""));

        HBox actions = new HBox(12, save, feedback);
        actions.setAlignment(Pos.CENTER_LEFT);
        grid.add(actions, 1, 6, 3, 1);
        return grid;
    }

    private GridPane buildEditCityForm(ResourceOptimizationView city) {
        TextField cityId = readOnlyText(city.cityId().toString());
        TextField cityName = new TextField(city.cityName());
        TextField activeCases = new TextField(city.activeCases().toString());
        TextField newCases = new TextField(city.totalNewCases().toString());
        TextField beds = new TextField(city.availableBeds().toString());
        TextField icu = new TextField(city.availableIcuBeds().toString());
        TextField ventilators = new TextField(city.availableVentilators().toString());
        TextField oxygen = new TextField(city.availableOxygenCylinders().toString());
        TextField vaccineDoses = new TextField(city.availableVaccineDoses().toString());
        Label feedback = new Label();
        Button save = new Button("Update City Data");

        save.setOnAction(event -> runManualSave(feedback, () -> {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("cityName", requiredText(cityName, "City name"));
            payload.put("newCases", requiredInteger(newCases, "New cases"));
            payload.put("activeCases", requiredInteger(activeCases, "Active cases"));
            payload.put("beds", requiredInteger(beds, "Beds"));
            putOptionalInteger(payload, "icu", icu);
            putOptionalInteger(payload, "ventilators", ventilators);
            putOptionalInteger(payload, "oxygen", oxygen);
            putOptionalInteger(payload, "vaccineDoses", vaccineDoses);
            payload.put("vaccineName", "PanVax Booster");
            return apiClient.updateCitySnapshot(city.cityId(), payload);
        }));

        GridPane grid = new GridPane();
        grid.getStyleClass().add("entry-grid");
        grid.setHgap(14);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        addRow(grid, 0, "City ID", cityId, "City name", cityName);
        addRow(grid, 1, "Active cases", activeCases, "New cases", newCases);
        addRow(grid, 2, "Beds", beds, "ICU", icu);
        addRow(grid, 3, "Ventilators", ventilators, "Oxygen", oxygen);
        addRow(grid, 4, "Vaccine dose", vaccineDoses, "", new Label(""));

        HBox actions = new HBox(12, save, feedback);
        actions.setAlignment(Pos.CENTER_LEFT);
        grid.add(actions, 1, 5, 3, 1);
        return grid;
    }

    private TextField readOnlyText(String value) {
        TextField field = new TextField(value);
        field.setEditable(false);
        field.setFocusTraversable(false);
        return field;
    }

    private void addWideRow(GridPane grid, int row, String labelText, javafx.scene.Node input) {
        Label label = new Label(labelText);
        GridPane.setHgrow(input, Priority.ALWAYS);
        grid.add(label, 0, row);
        grid.add(input, 1, row, 3, 1);
    }

    private void addRow(GridPane grid, int row, String leftLabel, TextField leftInput, String rightLabel, javafx.scene.Node rightInput) {
        addRow(grid, row, leftLabel, (javafx.scene.Node) leftInput, rightLabel, rightInput);
    }

    private void addRow(GridPane grid, int row, String leftLabel, javafx.scene.Node leftInput, String rightLabel, javafx.scene.Node rightInput) {
        Label first = new Label(leftLabel);
        Label second = new Label(rightLabel);
        GridPane.setHgrow(leftInput, Priority.ALWAYS);
        GridPane.setHgrow(rightInput, Priority.ALWAYS);
        grid.add(first, 0, row);
        grid.add(leftInput, 1, row);
        grid.add(second, 2, row);
        grid.add(rightInput, 3, row);
    }

    private void runManualSave(Label feedback, SaveAction action) {
        try {
            setLoading(true, "Saving city data...");
            feedback.setText("Saving...");
            action.run().whenComplete((ignored, throwable) -> Platform.runLater(() -> {
                setLoading(false, "Ready");
                if (throwable != null) {
                    feedback.setText(rootMessage(throwable));
                    statusLabel.setText(rootMessage(throwable));
                    return;
                }
                feedback.setText("Saved");
                statusLabel.setText("City data saved");
                loadStates();
            }));
        } catch (RuntimeException exception) {
            setLoading(false, "Ready");
            feedback.setText(exception.getMessage());
        }
    }

    private void applyRows(List<ResourceOptimizationView> rows) {
        tableView.setItems(FXCollections.observableArrayList(rows));

        long activeCases = rows.stream().mapToLong(row -> value(row.activeCases())).sum();
        long newCases = rows.stream().mapToLong(row -> value(row.totalNewCases())).sum();
        long beds = rows.stream().mapToLong(row -> value(row.availableBeds())).sum();
        long vaccines = rows.stream().mapToLong(row -> value(row.availableVaccineDoses())).sum();
        long critical = rows.stream().filter(row -> "CRITICAL".equals(row.riskStatus())).count();

        totalCitiesLabel.setText(format(rows.size()));
        activeCasesLabel.setText(format(activeCases));
        newCasesLabel.setText(format(newCases));
        bedsLabel.setText(format(beds));
        vaccineLabel.setText(format(vaccines));
        criticalLabel.setText(format(critical));
        statusLabel.setText("Loaded " + rows.size() + " cities");
    }

    private long value(Number number) {
        return number == null ? 0 : number.longValue();
    }

    private String format(long value) {
        return String.format("%,d", value);
    }

    private void setLoading(boolean loading, String status) {
        refreshButton.setDisable(loading);
        addCityButton.setDisable(loading);
        editCityButton.setDisable(loading);
        stateComboBox.setDisable(loading);
        progressIndicator.setVisible(loading);
        statusLabel.setText(status);
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage();
    }

    private String requiredText(TextField field, String label) {
        String value = field.getText().trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value;
    }

    private Long requiredLocationId(ComboBox<LocationOption> comboBox, String label) {
        LocationOption value = comboBox.getValue();
        if (value == null) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value.id();
    }

    private Integer requiredInteger(TextField field, String label) {
        try {
            return Integer.parseInt(requiredText(field, label));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " must be a whole number");
        }
    }

    private void putOptionalLong(Map<String, Object> payload, String key, TextField field) {
        if (!field.getText().isBlank()) {
            try {
                payload.put(key, Long.parseLong(field.getText().trim()));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(key + " must be a whole number");
            }
        }
    }

    private void putOptionalInteger(Map<String, Object> payload, String key, TextField field) {
        if (!field.getText().isBlank()) {
            payload.put(key, requiredInteger(field, key));
        }
    }

    private void putOptionalDecimal(Map<String, Object> payload, String key, TextField field) {
        if (!field.getText().isBlank()) {
            try {
                payload.put(key, new BigDecimal(field.getText().trim()));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(key + " must be numeric");
            }
        }
    }

    private StringConverter<LocationOption> locationConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(LocationOption location) {
                return location == null ? "" : location.name() + " (" + location.id() + ")";
            }

            @Override
            public LocationOption fromString(String value) {
                return null;
            }
        };
    }

    private TableColumn<ResourceOptimizationView, String> textColumn(
            String title,
            ValueExtractor<ResourceOptimizationView, String> extractor
    ) {
        TableColumn<ResourceOptimizationView, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(extractor.get(cell.getValue())));
        column.setMinWidth(110);
        return column;
    }

    private TableColumn<ResourceOptimizationView, Number> numberColumn(
            String title,
            ValueExtractor<ResourceOptimizationView, ? extends Number> extractor
    ) {
        TableColumn<ResourceOptimizationView, Number> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(extractor.get(cell.getValue())));
        column.setCellFactory(view -> rightAlignedCell());
        column.setMinWidth(82);
        return column;
    }

    private TableColumn<ResourceOptimizationView, BigDecimal> decimalColumn(
            String title,
            ValueExtractor<ResourceOptimizationView, BigDecimal> extractor
    ) {
        TableColumn<ResourceOptimizationView, BigDecimal> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(extractor.get(cell.getValue())));
        column.setCellFactory(view -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : value.toPlainString());
                setAlignment(Pos.CENTER_RIGHT);
            }
        });
        column.setMinWidth(108);
        return column;
    }

    private TableCell<ResourceOptimizationView, Number> rightAlignedCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("%,d", value.longValue()));
                setAlignment(Pos.CENTER_RIGHT);
            }
        };
    }

    @FunctionalInterface
    private interface SaveAction {
        CompletableFuture<Void> run();
    }

    @FunctionalInterface
    private interface ValueExtractor<T, R> {
        R get(T value);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
