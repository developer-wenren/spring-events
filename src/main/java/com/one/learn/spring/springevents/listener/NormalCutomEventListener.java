package com.one.learn.spring.springevents.listener;

import com.one.learn.spring.springevents.event.CustomEvent;
import org.springframework.context.ApplicationListener;

/**
 * 自定义事件监听
 *
 * @author One
 * @date 2019/05/26
 */
public class NormalCutomEventListener implements ApplicationListener<CustomEvent> {
    @Override
    public void onApplicationEvent(CustomEvent event) {
        System.out.println(Thread.currentThread() + "NormalCustomEventListener接受到自定义事件：" + event);
    }
}
