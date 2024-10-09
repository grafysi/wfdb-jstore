package com.grafysi.wfdbconsole.controller;

import com.grafysi.wfdb.driver.Connection;
import com.grafysi.wfdb.driver.MetadataConnection;
import com.grafysi.wfdb.driver.SignalConnection;
import com.grafysi.wfdbconsole.core.Controller;
import com.grafysi.wfdbconsole.core.View;
import com.grafysi.wfdbconsole.model.ReportModel;
import com.grafysi.wfdbconsole.utils.Utils;
import io.graphys.wfdbjstore.protocol.description.Description;
import io.graphys.wfdbjstore.protocol.exchange.CommandType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class WFDBConsoleController implements Controller, Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(WFDBConsoleController.class);

    private static final String SIGNAL_SESSION_TYPE = "SIGNAL_SESSION";

    private static final String METADATA_SESSION_TYPE = "METADATA_SESSION";

    private static final String CONNECTED_STATUS = "CONNECTED";

    private static final String CLOSED_STATUS = "CLOSED";

    @FXML
    private Text connectionInfoText;

    @FXML
    private Text connectionNameText;

    @FXML
    private Button disconnectButton;

    @FXML
    private Text sessionTypeText;

    @FXML
    private Text statusText;

    @FXML
    private MenuButton commandTypeMenuButton;

    @FXML
    private GridPane commandDescriptionGridPane;

    @FXML
    private Button executeButton;

    @FXML
    private SplitPane consoleSplitPane;

    private String connectionName;

    private Connection connection;

    private String sessionType;

    private Map<String, TextField> descriptionTextFields;

    private CommandType currentCommandType;

    private ReportViewerController reportViewerController;

    @Setter
    private Consumer<String> connectionClosedConsumer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        executeButton.setOnAction(this::handleExecuteButtonClicked);
        disconnectButton.setOnAction(this::handleDisconnectButtonClicked);
        initReportViewer();
    }

    private void initConsoleSplitPane() {
        //consoleSplitPane.setDividerPosition();
    }

    private void initReportViewer() {
        var view = View.of(ReportViewerController.class, VBox.class);
        view.component().setMaxSize(2000, 2000);
        reportViewerController = view.controller();
        consoleSplitPane.getItems().removeLast();
        consoleSplitPane.getItems().add(view.component());
    }

    public void setupConnection(String name, Connection connection) {
        this.connectionName = name;
        this.connection = connection;
        sessionType = switch (connection) {
            case MetadataConnection c -> METADATA_SESSION_TYPE;
            case SignalConnection c -> SIGNAL_SESSION_TYPE;
            default -> throw new IllegalArgumentException("Unexpected connection type: " + connection.getClass().getName());
        };
    }

    private String getConnectionStatus() {
        return connection.isClosed() ? "CLOSED" : "CONNECTED";
    }

    private String getConnectionInfo() {
        var dbInfo = "Database: " + connection.getDatabase() + "-v" + connection.getDbVersion();
        return sessionType.equals(SIGNAL_SESSION_TYPE)
                ? dbInfo + " | Record: " + ((SignalConnection) connection).getRecordName()
                : dbInfo;
    }

    private String getConnectionName() {
        return connectionName;
    }

    private String getSessionType() {
        return sessionType;
    }

    @Override
    public void postInitialize() {
        connectionNameText.setText(getConnectionName());
        statusText.setText(getConnectionStatus());
        sessionTypeText.setText(getSessionType());
        connectionInfoText.setText(getConnectionInfo());
        initCommandTypeMenuButton();

    }

    public void syncState() {
        statusText.setText(connection.isClosed() ? CLOSED_STATUS : CONNECTED_STATUS);
    }

    private void handleDisconnectButtonClicked(ActionEvent event) {
        connection.close();
        statusText.setText(CLOSED_STATUS);
        connectionClosedConsumer.accept(connectionName);
        Utils.alertInfo("Connection " + connectionName + " closed.");
    }

    private void initCommandTypeMenuButton() {
        commandTypeMenuButton.getItems().clear();
        connection.allowedCommandTypes().forEach(this::addCommandTypeMenuItem);
        commandTypeMenuButton.getItems().getFirst().fire();
    }

    private void addCommandTypeMenuItem(CommandType commandType) {
        var menuItem = new MenuItem(commandType.name());
        menuItem.setOnAction(this::handleCommandTypeMenuItemClicked);
        commandTypeMenuButton.getItems().add(menuItem);
    }

    private void handleCommandTypeMenuItemClicked(ActionEvent event) {

        var menuItem = (MenuItem) event.getSource();

        currentCommandType = CommandType.valueOf(menuItem.getText());

        commandTypeMenuButton.setText(currentCommandType.name());

        commandDescriptionGridPane.getChildren().clear();

        var rowIndex = new AtomicInteger(0);

        commandDescriptionGridPane.addRow(rowIndex.getAndIncrement(), new Label("CommandType"), commandTypeMenuButton);

        descriptionTextFields = new HashMap<>();

        Arrays.stream(currentCommandType.getDescriptionClass().getDeclaredFields())
                .forEach(field -> {
                    LOGGER.info("Description field: {}", field.getName());
                    var label = new Label(field.getName());
                    var input = new TextField();
                    commandDescriptionGridPane.addRow(rowIndex.getAndIncrement(), label, input);
                    descriptionTextFields.put(field.getName(), input);
                });

        commandDescriptionGridPane.add(executeButton, 1, rowIndex.getAndIncrement());
    }

    private void handleExecuteButtonClicked(ActionEvent event) {

        var fields = currentCommandType.getDescriptionClass().getDeclaredFields();
        var paramTypes = new Class<?>[fields.length];
        var paramValues = new Object[fields.length];

        for (int i = 0; i < fields.length; i++) {

            var field = fields[i];
            var fieldName = field.getName();
            var fieldType = field.getType();

            paramTypes[i] = fieldType;

            var input = descriptionTextFields.get(fieldName);

            if (fieldType.equals(String.class)) {

                var value = Utils.stringFrom(input, false).orElse(null);
                paramValues[i] = value;

            } else if (fieldType.equals(Integer.class)) {

                var value = Utils.intFrom(input, true, fieldName).orElse(null);
                paramValues[i] = value;

            } else if (fieldType.equals(Long.class)) {

                var value = Utils.longFrom(input, true, fieldName).orElse(null);
                paramValues[i] = value;

            } else if (fieldType.isArray() && fieldType.getComponentType().equals(String.class)) {

                var rawStr = Utils.stringFrom(input, false).orElse(null);
                var value = rawStr == null ? null : rawStr.split(",");
                paramValues[i] = value;

            } else {
                Utils.alertError("Unsupported description field type");
                return;
            }
        }

        try {
            var constructor = currentCommandType.getDescriptionClass().getDeclaredConstructor(paramTypes);

            var description = (Description) constructor.newInstance(paramValues);

            var start = Instant.now();
            var report = connection.executeCommand(currentCommandType, description);
            var end = Instant.now();

            var reportModel = ReportModel.builder()
                    .commandType(report.getCommandType())
                    .description(description)
                    .statusCode(report.getStatusCode())
                    .failedMessage(report.getFailedMessage())
                    .contents(report.getContentList())
                    .executionTime(Duration.between(start, end))
                    .build();

            reportViewerController.processReportModel(reportModel);

        } catch (Exception e) {

            LOGGER.warn("Exception thrown", e);
            Utils.alertError(e.getMessage());
        }
    }



}





































