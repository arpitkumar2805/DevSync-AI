package com.devsync.common.event;

public final class EventConstants {
    private EventConstants() {}

    public static final String EXCHANGE = "devsync.events";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String ANALYTICS_QUEUE = "analytics.queue";

    // Routing keys
    public static final String TASK_CREATED = "task.created";
    public static final String TASK_UPDATED = "task.updated";
    public static final String TASK_ASSIGNED = "task.assigned";
    public static final String SPRINT_STARTED = "sprint.started";
    public static final String SPRINT_CLOSED = "sprint.closed";
    public static final String COMMENT_ADDED = "comment.added";
    public static final String MEMBER_INVITED = "member.invited";
}
