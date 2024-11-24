package com.hivemq.tk;

import org.jetbrains.annotations.NotNull;

public interface Job {
    RunStatus RUNNING_STATUS = () -> false;
    RunStatus TERMINATING_STATUS = () -> true;

    default void drain(int workerId) {
        while (true) {
            if (!run(workerId)) {
                return;
            }
        }
    }

    /**
     * Runs and returns true if it should be rescheduled ASAP.
     *
     * @param workerId  worker id
     * @param runStatus set to 1 when job is running, 2 when it is halting
     * @return true if job should be rescheduled ASAP
     */
    boolean run(int workerId, @NotNull RunStatus runStatus);

    /**
     * Runs and returns true if it should be rescheduled ASAP.
     *
     * @param workerId worker id
     * @return true if job should be rescheduled ASAP
     */
    default boolean run(int workerId) {
        return run(workerId, RUNNING_STATUS);
    }

    interface RunStatus {
        boolean isTerminating();
    }
}
