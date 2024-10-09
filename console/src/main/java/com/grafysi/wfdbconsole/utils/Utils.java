package com.grafysi.wfdbconsole.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;

public class Utils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
    }

    public static Optional<String> stringFrom(TextField textField, boolean blankAlert, String fieldName) {

        var value = Optional.ofNullable(
                textField.getText() == null || textField.getText().isBlank() ? null : textField.getText());

        if (value.isEmpty() && blankAlert) {
            var alertText = fieldName == null
                    ? "Field is empty"
                    : fieldName + " field is empty";
            alertError(alertText);
        }

        return value;
    }

    public static Optional<String> stringFrom(TextField textField, boolean blankWarn) {
        return stringFrom(textField, blankWarn, null);
    }

    public static Optional<Integer> intFrom(TextField textField, boolean invalidAlert, String fieldName) {

        try {
            var strValue = stringFrom(textField, false).orElse(null);

            if (strValue == null) {
                return Optional.empty();
            }
            return Optional.of(Integer.parseInt(strValue));

        } catch (NumberFormatException e) {

            if (invalidAlert) {
                var alertText = fieldName == null
                        ? "Field is not an valid integer"
                        : fieldName + " field is not an valid integer";
                alertError(alertText);
            }
            return Optional.empty();
        }
    }

    public static Optional<Integer> intFrom(TextField textField, boolean invalidAlert) {
        return intFrom(textField, invalidAlert, null);
    }

    public static Optional<Long> longFrom(TextField textField, boolean invalidAlert, String fieldName) {

        try {
            var strValue = stringFrom(textField, false).orElse(null);

            if (strValue == null) {
                return Optional.empty();
            }
            return Optional.of(Long.parseLong(strValue));

        } catch (NumberFormatException e) {

            if (invalidAlert) {
                var alertText = fieldName == null
                        ? "Field is not an valid integer"
                        : fieldName + " field is not an valid integer";
                alertError(alertText);
            }
            return Optional.empty();
        }
    }

    public static Optional<Long> longFrom(TextField textField, boolean invalidAlert) {
        return longFrom(textField, invalidAlert, null);
    }


    public static void alertError(String errorText) {

        var alter = new Alert(Alert.AlertType.ERROR);
        alter.setTitle("Error alert");
        alter.setContentText(errorText);
        alter.showAndWait();
    }

    public static void alertInfo(String infoText) {

        var alter = new Alert(Alert.AlertType.INFORMATION);
        alter.setTitle("Info alert");
        alter.setContentText(infoText);
        alter.showAndWait();
    }

    public static String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String toPrettyJsonString(Object object) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <S, T> TableColumn<S, T> createPropertyColumn(Class<S> modelType, Class<T> propertyType,
                                                                String columnName, String propertyName, double minWidth) {
        var column = new TableColumn<S, T>(columnName);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setMinWidth(minWidth);
        return column;
    }
}


























