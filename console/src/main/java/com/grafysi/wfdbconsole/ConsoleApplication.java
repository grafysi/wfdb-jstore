package com.grafysi.wfdbconsole;

import com.grafysi.wfdbconsole.controller.ConsoleAppController;
import com.grafysi.wfdbconsole.core.View;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ConsoleApplication extends Application {

    @Override
    public void start(Stage primaryStage) {

        var view = View.of(ConsoleAppController.class, VBox.class);

        primaryStage.setScene(new Scene(view.component()));

        primaryStage.getScene().getStylesheets().add(getClass().getClassLoader().getResource("main.css").toExternalForm());

        primaryStage.show();
    }
}
