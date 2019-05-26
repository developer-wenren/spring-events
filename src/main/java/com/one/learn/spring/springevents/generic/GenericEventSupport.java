package com.one.learn.spring.springevents.generic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author One
 */
@Component
public class GenericEventSupport {
    private final ApplicationEventPublisher publisher;

    @Autowired
    public GenericEventSupport(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void createOrder(Order order, boolean awesome) {
        this.publisher.publishEvent(new OrderCreatedEvent(order, awesome));
    }

    @EventListener(condition = "#creationEvent.awesome")
    public void handleOrderCreatedEvent(CreationEvent<Order> creationEvent) {
        System.out.println("处理订单新增事件: " + creationEvent);
    }
}