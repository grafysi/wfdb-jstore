package com.grafysi.wfdbconsole.controller;

import com.grafysi.wfdb.driver.Connection;
import com.grafysi.wfdbconsole.core.Controller;
import com.grafysi.wfdbconsole.core.View;
import com.grafysi.wfdbconsole.model.ConnectionInitModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

public class ConsoleAppController implements Controller, Initializable {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 18080;
    private static final String DEFAULT_DATABASE = "mimic4wdb";
    private static final String DEFAULT_DB_VERSION = "0.1.0";

    @FXML
    private Button addConnectionButton;

    @FXML
    private MenuButton newConnectionTabMenuButton;

    @FXML
    private TabPane consoleTabPane;

    private final Map<String, Connection> connectionMap = new HashMap<>();

    private ConnectionInitModel initModelCache;

    private final AtomicInteger CONNECTION_INDEX = new AtomicInteger(1);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addConnectionButton.setOnAction(this::handleAddConnectionButtonClicked);
        initNewConnectionTabMenuButton();
        initConsoleTabPane();
    }

    private void initNewConnectionTabMenuButton() {
        newConnectionTabMenuButton.getItems().clear();
    }

    private void initConsoleTabPane() {
        consoleTabPane.getTabs().clear();
    }

    private void setInitModelCache(ConnectionInitModel model) {
        this.initModelCache = model;
    }

    private void handleAddConnectionButtonClicked(ActionEvent event) {

        var view = View.of(InitConnectionController.class, VBox.class);

        view.controller().setConnectionConsumer(this::consumeNewConnections);
        view.controller().setConnectionInitModelConsumer(this::setInitModelCache);
        view.controller().fillCachedValues(getInitModelCache());
        view.controller().postInitialize();

        var stage = new Stage();
        stage.setTitle("Initialize connection");
        stage.setScene(new Scene(view.component()));
        stage.showAndWait();
    }

    private String createInitConnectionName() {
        return "Connection #" + CONNECTION_INDEX.getAndIncrement();
    }

    private ConnectionInitModel getInitModelCache() {
        if (initModelCache == null) {
            return ConnectionInitModel.builder()
                    .name(createInitConnectionName())
                    .host(DEFAULT_HOST)
                    .port(DEFAULT_PORT)
                    .database(DEFAULT_DATABASE)
                    .dbVersion(DEFAULT_DB_VERSION)
                    .build();
        }
        return ConnectionInitModel.builder()
                .name(createInitConnectionName())
                .host(initModelCache.host() == null ? DEFAULT_HOST : initModelCache.host())
                .port(initModelCache.port() == null ? DEFAULT_PORT : initModelCache.port())
                .user(initModelCache.user() == null ? null : initModelCache.user())
                .password(initModelCache.password() == null ? null : initModelCache.password())
                .database(initModelCache.database() == null ? DEFAULT_DATABASE : initModelCache.database())
                .dbVersion(initModelCache.dbVersion() == null ? DEFAULT_DB_VERSION : initModelCache.dbVersion())
                .build();
    }

    private void consumeNewConnections(String name, Connection connection) {

        var normalizedName = normalizeName(name);
        connectionMap.put(normalizedName, connection);

        var menuItem = new MenuItem(normalizedName);
        menuItem.setOnAction(this::handleConnectionMenuItemClicked);

        newConnectionTabMenuButton.getItems().add(menuItem);
        menuItem.fire();
    }

    private String normalizeName(String name) {
        if (!connectionMap.containsKey(name)) {
            return name;
        }
        return normalizeName(name + "-1");
    }

    private void handleConnectionMenuItemClicked(ActionEvent event) {

        var menuItem = (MenuItem) event.getSource();
        var name = menuItem.getText();

        var connection = connectionMap.get(name);
        if (connection == null) {
            throw new RuntimeException("Unexpected error. Cannot get connection with name: " + name);
        }

        var view = View.of(WFDBConsoleController.class, VBox.class);

        view.controller().setupConnection(name, connection);
        view.controller().setConnectionClosedConsumer(deletedName -> {
            connectionMap.remove(deletedName);
            var deletedItems = newConnectionTabMenuButton.getItems()
                    .stream()
                    .filter(item -> item.getText().equals(deletedName))
                    .toList();
            newConnectionTabMenuButton.getItems().removeAll(deletedItems);
        });
        view.controller().postInitialize();

        view.component().setMaxSize(2000, 2000);
        view.component().setMinSize(200, 200);

        var tab = new Tab(name, view.component());
        tab.setClosable(true);
        tab.setOnSelectionChanged(e -> view.controller().syncState());

        consoleTabPane.getTabs().add(tab);
        consoleTabPane.getSelectionModel().selectLast();
    }
}




































