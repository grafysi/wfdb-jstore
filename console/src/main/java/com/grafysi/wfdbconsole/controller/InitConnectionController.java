package com.grafysi.wfdbconsole.controller;
import com.grafysi.wfdb.driver.Connection;
import com.grafysi.wfdb.driver.DriverManager;
import com.grafysi.wfdb.driver.exception.WfdbException;
import com.grafysi.wfdbconsole.core.Controller;
import com.grafysi.wfdbconsole.model.ConnectionInitModel;
import com.grafysi.wfdbconsole.utils.Utils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class InitConnectionController implements Controller, Initializable {

    private static final String SIGNAL_CONNECTION_TYPE = "SIGNAL";

    private static final String METADATA_CONNECTION_TYPE = "METADATA";

    @FXML
    private Button applyButton;

    @FXML
    private Button cancelButton;

    @FXML
    private TextField nameTextField;

    @FXML
    private SplitMenuButton connectionTypeButton;

    @FXML
    private TextField databaseTextField;

    @FXML
    private TextField hostTextField;

    @FXML
    private Button okButton;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private TextField portTextField;

    @FXML
    private TextField recordTextField;

    @FXML
    private TextField userTextField;

    @FXML
    private TextField versionTextField;

    @Setter @Getter
    private BiConsumer<String, Connection> connectionConsumer;

    private String currentConnectionType;

    @Setter
    private Consumer<ConnectionInitModel> connectionInitModelConsumer;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeConnectionTypeButton();
        okButton.setOnAction(this::handleOkButtonClicked);
    }

    @Override
    public void postInitialize() {

    }

    public void fillCachedValues(ConnectionInitModel model) {

        if (model.name() != null) {
            nameTextField.setText(model.name());
        }

        if (model.host() != null) {
            hostTextField.setText(model.host());
        }

        if (model.port() != null) {
            portTextField.setText(String.valueOf(model.port()));
        }

        if (model.user() != null) {
            userTextField.setText(model.user());
        }

        if (model.password() != null) {
            passwordTextField.setText(model.password());
        }

        if (model.database() != null) {
            databaseTextField.setText(model.database());
        }

        if (model.dbVersion() != null) {
            versionTextField.setText(model.dbVersion());
        }

    }

    private void handleOkButtonClicked(ActionEvent event) {

        try {
            var model = loadConnectionInitModel();

            Connection connection;

            if (currentConnectionType.equals(METADATA_CONNECTION_TYPE)) {

                connection = DriverManager.newMetadataConnection(
                        model.host(), model.port(), model.user(), model.password(), model.database(), model.dbVersion());

            } else if (currentConnectionType.equals(SIGNAL_CONNECTION_TYPE)) {

                final var record = Utils.stringFrom(recordTextField, true, "Record name").orElseThrow();
                connection = DriverManager.newSignalConnection(
                        model.host(), model.port(), model.user(), model.password(), model.database(), model.dbVersion(), record);

            } else {

                throw new WfdbException("Unexpected error");
            }

            registerFXTask(() -> connectionConsumer.accept(model.name(), connection));

            connectionInitModelConsumer.accept(model);

            closeWindowOfEvent(event);

        } catch (NoSuchElementException e) {
            // ignore
        } catch (WfdbException e) {
            Utils.alertError("Connect failed with message: " + e.getMessage());
        }
    }

    private ConnectionInitModel loadConnectionInitModel() {

        final var name = Utils.stringFrom(nameTextField, true, "Connection name").orElseThrow();

        final var type = currentConnectionType;

        final var host = Utils.stringFrom(hostTextField, true, "Host").orElseThrow();

        final int port = Utils.intFrom(portTextField, true, "Port").orElseThrow();

        final var user = Utils.stringFrom(userTextField, true, "User").orElseThrow();

        final var password = Utils.stringFrom(passwordTextField, true, "Password").orElseThrow();

        final var database = Utils.stringFrom(databaseTextField, true, "Database").orElseThrow();

        final var dbVersion = Utils.stringFrom(versionTextField, true, "Database version").orElseThrow();

        return ConnectionInitModel.builder()
                .name(name)
                .host(host)
                .port(port)
                .user(user)
                .password(password)
                .database(database)
                .database(database)
                .dbVersion(dbVersion)
                .build();
    }

    private void closeWindowOfEvent(Event event) {
        var stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void initializeConnectionTypeButton() {
        connectionTypeButton.getItems().clear();
        connectionTypeButton.getItems().add(new MenuItem(METADATA_CONNECTION_TYPE));
        connectionTypeButton.getItems().add(new MenuItem(SIGNAL_CONNECTION_TYPE));
        connectionTypeButton.getItems().forEach(item -> item.setOnAction(this::handleConnectionTypeSelected));
        connectionTypeButton.getItems().getFirst().fire();
    }

    private void handleConnectionTypeSelected(ActionEvent event) {

        var selection = ((MenuItem) event.getSource()).getText();
        currentConnectionType = selection;
        connectionTypeButton.setText(selection);

        if (selection.equals(METADATA_CONNECTION_TYPE)) {
            recordTextField.setText(null);
            recordTextField.setDisable(true);
        } else if (selection.equals(SIGNAL_CONNECTION_TYPE)) {
            recordTextField.setDisable(false);
        }
    }





}




























