package com.one.learn.spring.springevents.listener;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 初始化侦听器
 *
 * @author One
 * @date 2019/05/26
 */
@Component
public class InitalizeListener implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        System.out.println("Spring 容器启动  获取到 Application Context 对象 " + applicationContext);
    }

    @EventListener
    public void listener(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        System.out.println("Spring 容器启动   注解方式侦听事件： " + applicationContext);
    }
}
