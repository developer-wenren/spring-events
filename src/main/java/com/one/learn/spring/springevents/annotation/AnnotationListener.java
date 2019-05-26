package com.one.learn.spring.springevents.annotation;

import com.one.learn.spring.springevents.event.SecondCustomEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.RequestHandledEvent;

/**
 * 注解事件监听器
 *
 * @author One
 * @date 2019/05/26
 */
@Component
public class AnnotationListener {

    @EventListener(value = {ContextRefreshedEvent.class, ContextStartedEvent.class, ContextStoppedEvent.class,
            ContextClosedEvent.class, RequestHandledEvent.class}, condition = "#root.event != null")
    public void listener(ApplicationEvent event) {
        System.out.println(Thread.currentThread() + " 接收到 Spring 事件：" + event);
    }
}
