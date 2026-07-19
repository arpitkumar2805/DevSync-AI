package com.devsync.task.config;

import com.devsync.common.event.EventConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange devsyncExchange() {
        return new TopicExchange(EventConstants.EXCHANGE);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(EventConstants.NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Queue analyticsQueue() {
        return QueueBuilder.durable(EventConstants.ANALYTICS_QUEUE).build();
    }

    @Bean
    public Binding notificationTaskCreated(Queue notificationQueue, TopicExchange devsyncExchange) {
        return BindingBuilder.bind(notificationQueue).to(devsyncExchange).with("task.*");
    }

    @Bean
    public Binding notificationSprint(Queue notificationQueue, TopicExchange devsyncExchange) {
        return BindingBuilder.bind(notificationQueue).to(devsyncExchange).with("sprint.*");
    }

    @Bean
    public Binding notificationComment(Queue notificationQueue, TopicExchange devsyncExchange) {
        return BindingBuilder.bind(notificationQueue).to(devsyncExchange).with("comment.*");
    }

    @Bean
    public Binding notificationMember(Queue notificationQueue, TopicExchange devsyncExchange) {
        return BindingBuilder.bind(notificationQueue).to(devsyncExchange).with("member.*");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
