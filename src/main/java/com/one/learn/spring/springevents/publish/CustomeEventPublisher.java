package com.one.learn.spring.springevents.publish;

import com.one.learn.spring.springevents.event.CustomEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

/**
 * 自定义事件发布者
 *
 * @author One
 * @date 2019/05/26
 */
@Component
public class CustomeEventPublisher implements ApplicationEventPublisherAware {
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishEvent(String message) {
        System.out.println("开始发布事件 " + message);
        applicationEventPublisher.publishEvent(new CustomEvent(this, message));
    }
}
