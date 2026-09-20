package com.devsync.common.enums;

public enum TaskStatus {
    BACKLOG,
    TODO,
    IN_PROGRESS,
    REVIEW,
    DONE;

    /**
     * Validates whether a status transition is allowed.
     * BACKLOG → TODO → IN_PROGRESS → REVIEW → DONE
     * Also allows: IN_PROGRESS → BACKLOG (send back), REVIEW → IN_PROGRESS (rework)
     */
    public boolean canTransitionTo(TaskStatus target) {
        return switch (this) {
            case BACKLOG -> target == TODO;
            case TODO -> target == IN_PROGRESS || target == BACKLOG;
            case IN_PROGRESS -> target == REVIEW || target == BACKLOG;
            case REVIEW -> target == DONE || target == IN_PROGRESS;
            case DONE -> target == TODO; // reopen
        };
    }
}
