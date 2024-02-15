package com.school.torconfigtool.nginx.event;

import org.springframework.context.ApplicationEvent;

/**
 * This class represents an event that is published when Nginx needs to be reloaded.
 * It extends the ApplicationEvent class provided by the Spring Framework.
 *
 * The event can be published by any component within the application and can be listened to by any other component.
 * This allows for a loose coupling between the component that triggers the reload and the component that actually performs the reload.
 */
public class NginxReloadEvent extends ApplicationEvent {
    /**
     * Constructor for the NginxReloadEvent.
     *
     * @param source The object on which the event initially occurred.
     */
    public NginxReloadEvent(Object source) {
        super(source);
    }
}