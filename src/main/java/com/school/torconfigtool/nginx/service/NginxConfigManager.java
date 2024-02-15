package com.school.torconfigtool.nginx.service;

import com.school.torconfigtool.nginx.event.NginxReloadEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * This service class manages the configuration of Nginx.
 * It uses an instance of NginxConfigWriter to perform the actual configuration changes.
 */
@Service
public class NginxConfigManager {

    // An instance of NginxConfigWriter to perform the actual configuration changes
    private final NginxConfigWriter nginxConfigWriter;

    // An instance of ApplicationEventPublisher to publish events
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructor for the NginxConfigManager class.
     * @param nginxConfigWriter an instance of NginxConfigWriter
     * @param eventPublisher an instance of ApplicationEventPublisher
     */
    @Autowired
    public NginxConfigManager(NginxConfigWriter nginxConfigWriter, ApplicationEventPublisher eventPublisher) {
        this.nginxConfigWriter = nginxConfigWriter;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Changes the root directory of the Nginx configuration.
     * @param rootDirectory the new root directory
     */
    public void changeRootDirectory(String rootDirectory) {
        nginxConfigWriter.writeCommonConfig(rootDirectory);
    }

    /**
     * Reverts the Nginx configuration to its default state.
     * It also publishes an event to reload the Nginx configuration.
     */
    public void revertNginxDefaultConfig() {
        String rootDirectory = "/home/matys/git/torConfigTool/onion/www/service-80";
        nginxConfigWriter.writeCommonConfig(rootDirectory);
        eventPublisher.publishEvent(new NginxReloadEvent(this));
    }

    /**
     * Modifies the default Nginx configuration with the given parameters.
     * It also publishes an event to reload the Nginx configuration.
     * @param programLocation the location of the program
     * @param randomString a random string
     * @param webTunnelUrl the URL of the web tunnel
     */
    public void modifyNginxDefaultConfig(String programLocation, String randomString, String webTunnelUrl) {
        nginxConfigWriter.writeModifiedConfig(programLocation, randomString, webTunnelUrl);
        eventPublisher.publishEvent(new NginxReloadEvent(this));
    }
}