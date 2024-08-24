package io.graphys.wfdbjstore.engine;

public interface WfdbExecutor {

    default boolean isAllowedExecution(WfdbExecution execution) {
        return getExecutionClass().isAssignableFrom(execution.getClass());
    }

    Class<? extends WfdbExecution> getExecutionClass();


}
