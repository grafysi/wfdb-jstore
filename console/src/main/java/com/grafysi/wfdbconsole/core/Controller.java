package com.grafysi.wfdbconsole.core;

import javafx.application.Platform;

public interface Controller  {

    default void registerFXTask(Runnable runnable) {
        Platform.runLater(runnable);
    }

    default void postInitialize() {

    }

    default void destroy() {

    }
}
