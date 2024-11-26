package com.hivemq.tk;

import org.jetbrains.annotations.NotNull;

public interface Job {
    RunStatus RUNNING_STATUS = () -> false;
    RunStatus TERMINATING_STATUS = () -> true;

    default void drain(final int workerId) {
        while (true) {
            if (!run(workerId)) {
                return;
            }
        }
    }

    boolean run(final int workerId, final @NotNull RunStatus runStatus);

    default boolean run(final int workerId) {
        return run(workerId, RUNNING_STATUS);
    }

    default void bindProperties() {
        // no-op
    }

    @FunctionalInterface
    interface RunStatus {
        boolean isTerminating();
    }
}
