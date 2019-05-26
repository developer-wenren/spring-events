package com.one.learn.spring.springevents.listener;

import com.one.learn.spring.springevents.event.CustomEvent;
import com.one.learn.spring.springevents.event.SecondCustomEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 自定义事件监听
 *
 * @author One
 * @date 2019/05/26
 */
@Component
@Order(2)
public class CustomEventListener {
    @EventListener
    public SecondCustomEvent listener(CustomEvent event) {
        System.out.println(Thread.currentThread() + "CustomEventListener接受到自定义事件：" + event);
        return new SecondCustomEvent(this, event.toString());
    }
}
