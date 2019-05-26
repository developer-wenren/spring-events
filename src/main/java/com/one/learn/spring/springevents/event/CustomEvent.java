package com.one.learn.spring.springevents.event;

import org.springframework.context.ApplicationEvent;

/**
 * 自定义事件
 *
 * @author One
 * @date 2019/05/26
 */
public class CustomEvent extends ApplicationEvent {
    private String data;

    public CustomEvent(Object source, String data) {
        super(source);
        this.data = data;
    }

    public String getCustom() {
        return data;
    }

    public void setCustom(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CustomEvent{" +
                "data='" + data + '\'' +
                ", source=" + source +
                '}';
    }
}
