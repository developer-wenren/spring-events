package com.one.learn.spring.springevents.generic;

/**
 * @author One
 */
public class OrderCreatedEvent implements CreationEvent<Order> {

    private final Order order;
    private boolean awesome;

    public OrderCreatedEvent(Order order, boolean awesome) {
        this.order = order;
        this.awesome = awesome;
    }

    public OrderCreatedEvent(Order order) {
        this.order = order;
        this.awesome = true;
    }

    public boolean isAwesome() {
        return this.awesome;
    }

    @Override
    public String toString() {
        return "OrderCreatedEvent{" +
                "order=" + order +
                ", awesome=" + awesome +
                '}';
    }
}