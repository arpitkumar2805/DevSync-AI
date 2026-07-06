package com.devsync.common.enums;

public enum SprintStatus {
    PLANNED,
    ACTIVE,
    CLOSED;

    public boolean canTransitionTo(SprintStatus target) {
        return switch (this) {
            case PLANNED -> target == ACTIVE;
            case ACTIVE -> target == CLOSED;
            case CLOSED -> false;
        };
    }
}
