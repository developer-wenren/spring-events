package com.one.learn.spring.springevents.listener;

import com.one.learn.spring.springevents.event.SecondCustomEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 自定义事件监听
 *
 * @author One
 * @date 2019/05/26
 */
@Component
public class SecondCustomEventListener {
    @EventListener
    public void onApplicationEvent(SecondCustomEvent event) {
        System.out.println(Thread.currentThread() + " SecondCustomEventListener 接受到自定义事件：" + event);
    }
}
