package com.one.learn.spring.springevents.event;

import org.springframework.context.ApplicationEvent;

/**
 * 自定义事件
 *
 * @author One
 * @date 2019/05/26
 */
public class SecondCustomEvent extends ApplicationEvent {
    private String custom;

    public SecondCustomEvent(Object source, String custom) {
        super(source);
        this.custom = custom;
    }

    public String getCustom() {
        return custom;
    }

    public void setCustom(String custom) {
        this.custom = custom;
    }

    @Override
    public String toString() {
        return "SecondCustomEvent{" +
                "custom='" + custom + '\'' +
                ", source=" + source +
                '}';
    }
}
