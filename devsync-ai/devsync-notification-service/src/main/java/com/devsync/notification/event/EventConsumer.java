package com.devsync.notification.event;

import com.devsync.common.event.DevsyncEvent;
import com.devsync.common.event.EventConstants;
import com.devsync.notification.notification.entity.Notification;
import com.devsync.notification.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {

    private final NotificationRepository notificationRepository;

    @RabbitListener(queues = EventConstants.NOTIFICATION_QUEUE)
    public void handleEvent(DevsyncEvent event) {
        log.info("Received event: {} for entity: {}", event.getEventType(), event.getEntityId());

        try {
            Notification notification = Notification.builder()
                    .userId(event.getActorUserId())
                    .type(event.getEventType())
                    .title(event.getTitle())
                    .message(event.getMessage())
                    .entityType(event.getEntityType())
                    .entityId(event.getEntityId())
                    .read(false)
                    .build();

            notificationRepository.save(notification);
            log.info("Notification created for event: {}", event.getEventType());
        } catch (Exception e) {
            log.error("Failed to process event: {} - {}", event.getEventType(), e.getMessage());
        }
    }
}
