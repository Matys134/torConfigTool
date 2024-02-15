package com.school.torconfigtool;

import org.springframework.context.ApplicationEvent;

public class NginxReloadEvent extends ApplicationEvent {
    public NginxReloadEvent(Object source) {
        super(source);
    }
}