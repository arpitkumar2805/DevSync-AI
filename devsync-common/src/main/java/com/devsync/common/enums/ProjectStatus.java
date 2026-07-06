package com.devsync.common.enums;

public enum ProjectStatus {
    ACTIVE,
    ON_HOLD,
    COMPLETED,
    ARCHIVED;

    public boolean canTransitionTo(ProjectStatus target) {
        return switch (this) {
            case ACTIVE -> target == ON_HOLD || target == COMPLETED || target == ARCHIVED;
            case ON_HOLD -> target == ACTIVE || target == ARCHIVED;
            case COMPLETED -> target == ARCHIVED || target == ACTIVE;
            case ARCHIVED -> target == ACTIVE;
        };
    }
}
