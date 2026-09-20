package com.devsync.task.event;

import com.devsync.common.event.DevsyncEvent;
import com.devsync.common.event.EventConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishTaskCreated(UUID taskId, UUID actorId, String title) {
        publish(EventConstants.TASK_CREATED, taskId, "TASK", actorId, title, "New task created: " + title);
    }

    public void publishTaskUpdated(UUID taskId, UUID actorId, String title, String changes) {
        publish(EventConstants.TASK_UPDATED, taskId, "TASK", actorId, title, "Task updated: " + changes);
    }

    public void publishTaskAssigned(UUID taskId, UUID actorId, String title, String assigneeEmail) {
        publish(EventConstants.TASK_ASSIGNED, taskId, "TASK", actorId, title, "Task assigned to " + assigneeEmail);
    }

    public void publishSprintStarted(UUID sprintId, UUID actorId, String sprintName) {
        publish(EventConstants.SPRINT_STARTED, sprintId, "SPRINT", actorId, sprintName, "Sprint started: " + sprintName);
    }

    public void publishSprintClosed(UUID sprintId, UUID actorId, String sprintName) {
        publish(EventConstants.SPRINT_CLOSED, sprintId, "SPRINT", actorId, sprintName, "Sprint closed: " + sprintName);
    }

    public void publishCommentAdded(UUID taskId, UUID actorId, String taskTitle) {
        publish(EventConstants.COMMENT_ADDED, taskId, "COMMENT", actorId, taskTitle, "New comment on: " + taskTitle);
    }

    private void publish(String routingKey, UUID entityId, String entityType, UUID actorId, String title, String message) {
        DevsyncEvent event = DevsyncEvent.builder()
                .eventType(routingKey)
                .entityId(entityId)
                .entityType(entityType)
                .actorUserId(actorId)
                .title(title)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        try {
            rabbitTemplate.convertAndSend(EventConstants.EXCHANGE, routingKey, event);
            log.info("Published event: {} for entity: {}", routingKey, entityId);
        } catch (Exception e) {
            log.error("Failed to publish event: {} - {}", routingKey, e.getMessage());
        }
    }
}
