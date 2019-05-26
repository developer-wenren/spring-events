package com.one.learn.spring.springevents.listener;

import com.one.learn.spring.springevents.event.CustomEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 自定义事件监听
 *
 * @author One
 * @date 2019/05/26
 */
@Component
@Order(value = 1)
public class CustomSecondEventListener implements ApplicationListener<CustomEvent> {
    @Override
    @Async
    public void onApplicationEvent(CustomEvent event) {
        int i = 1 / 0;
        System.out.println(Thread.currentThread() + "CustomSecondEventListener接受到自定义事件：" + event);
    }
}
