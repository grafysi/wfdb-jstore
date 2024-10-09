package com.grafysi.wfdbconsole.component;

import com.grafysi.wfdbconsole.model.ReportModel;
import com.grafysi.wfdbconsole.utils.JsonViewer;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;

public class ActionTableCell extends TableCell<ReportModel, Void> {

    private final HBox container;

    private final Button viewButton;

    private final Button deleteButton;

    public ActionTableCell() {

        viewButton = new Button("{-}");
        viewButton.setOnAction(this::handleViewButtonClicked);

        deleteButton = new Button("[X]");
        deleteButton.setOnAction(this::handleDeleteButtonClicked);

        container = new HBox(viewButton, deleteButton);
        container.setSpacing(4);
    }

    private void handleViewButtonClicked(ActionEvent event) {
        var reportModel = getTableView().getItems().get(getIndex());
        var jsonViewer = new JsonViewer(reportModel);
        jsonViewer.showAndWait();
    }

    private void handleDeleteButtonClicked(ActionEvent event) {
        getTableView().getItems().remove(getIndex());
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            setGraphic(container);
        }
    }

}
