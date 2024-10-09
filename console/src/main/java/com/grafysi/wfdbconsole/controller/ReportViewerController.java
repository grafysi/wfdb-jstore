package com.grafysi.wfdbconsole.controller;

import com.grafysi.wfdbconsole.component.ActionTableCell;
import com.grafysi.wfdbconsole.core.Controller;
import com.grafysi.wfdbconsole.model.ReportModel;
import com.grafysi.wfdbconsole.utils.JsonViewer;
import com.grafysi.wfdbconsole.utils.Utils;
import io.graphys.wfdbjstore.protocol.content.Content;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.controlsfx.control.table.TableRowExpanderColumn;

import java.net.URL;
import java.util.ResourceBundle;

public class ReportViewerController implements Controller, Initializable {

    @FXML
    private TableView<ReportModel> reportTableView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initReportTableView();
    }

    public void processReportModel(ReportModel reportModel) {
        reportTableView.getItems().add(reportModel);
    }

    private void initReportTableView() {

        reportTableView.getColumns().clear();

        reportTableView.setItems(FXCollections.observableArrayList());

        var expanderColumn = new TableRowExpanderColumn<ReportModel>(param -> {

            var listView = new ListView<HBox>();
            param.getValue().contents().stream()
                    .map(content -> {
                        var hBox = new HBox();
                        var viewButton = new Button("{-}");
                        viewButton.setOnAction(createJsonViewHandler(content));
                        hBox.getChildren().add(viewButton);
                        hBox.getChildren().add(new Separator(Orientation.VERTICAL));
                        hBox.getChildren().add(new Text(Utils.toJsonString(content)));
                        return hBox;
                    })
                    .forEach(v -> listView.getItems().add(v));

            //final int ITEM_HEIGHT = 30;
            //listView.setFixedCellSize(ITEM_HEIGHT);
            //listView.setPrefHeight(ITEM_HEIGHT + ITEM_HEIGHT * param.getValue().contents().size());
            return new VBox(listView);
        });
        expanderColumn.setPrefWidth(24);
        reportTableView.getColumns().add(expanderColumn);

        reportTableView.getColumns().add(Utils.createPropertyColumn(
                ReportModel.class, String.class, "CmdType", "commandType", 200));

        reportTableView.getColumns().add(Utils.createPropertyColumn(
                ReportModel.class, String.class, "Description", "description", 250));

        reportTableView.getColumns().add(Utils.createPropertyColumn(
                ReportModel.class, String.class, "StatusCode", "statusCode", 120));

        reportTableView.getColumns().add(Utils.createPropertyColumn(
                ReportModel.class, String.class, "FailedMsg", "failedMessage", 100));

        reportTableView.getColumns().add(Utils.createPropertyColumn(
                ReportModel.class, String.class, "CntCount", "contentCount", 100));

        reportTableView.getColumns().add(Utils.createPropertyColumn(
                ReportModel.class, String.class, "ExecTime", "executionTime", 100));

        var actionColumn = new TableColumn<ReportModel, Void>("Action");
        actionColumn.setCellFactory(column -> new ActionTableCell());
        actionColumn.setMinWidth(120);
        reportTableView.getColumns().add(actionColumn);
    }

    private EventHandler<ActionEvent> createJsonViewHandler(Content content) {
        return event -> {
            var jsonViewer = new JsonViewer(content);
            jsonViewer.showAndWait();
        };
    }
}



































