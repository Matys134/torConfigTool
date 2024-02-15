package com.school.torconfigtool.nginx.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service class for managing Nginx operations.
 */
@Service
public class NginxService {

    private final NginxProcessService nginxProcessService;
    private final NginxConfigService nginxConfigService;

    /**
     * Constructor for NginxService.
     *
     * @param nginxProcessService Service for managing Nginx processes.
     * @param nginxConfigService Service for managing Nginx configuration.
     */
    @Autowired
    public NginxService(NginxProcessService nginxProcessService, NginxConfigService nginxConfigService) {
        this.nginxProcessService = nginxProcessService;
        this.nginxConfigService = nginxConfigService;
    }

    /**
     * Starts the Nginx service.
     */
    public void startNginx() {
        nginxProcessService.startNginx();
    }

    /**
     * Generates the Nginx configuration.
     */
    public void generateNginxConfig() {
        nginxConfigService.generateNginxConfig();
    }

    /**
     * Changes the root directory of the Nginx configuration.
     *
     * @param rootDirectory The new root directory.
     */
    public void changeRootDirectory(String rootDirectory) {
        nginxConfigService.changeRootDirectory(rootDirectory);
    }

    /**
     * Reverts the Nginx configuration to its default state.
     */
    public void revertNginxDefaultConfig() {
        nginxConfigService.revertNginxDefaultConfig();
    }

    /**
     * Modifies the default Nginx configuration.
     *
     * @param programLocation The location of the program.
     * @param randomString A random string.
     * @param webTunnelUrl The URL of the web tunnel.
     */
    public void modifyNginxDefaultConfig(String programLocation, String randomString, String webTunnelUrl) {
        nginxConfigService.modifyNginxDefaultConfig(programLocation, randomString, webTunnelUrl);
    }

    /**
     * Checks if the Nginx service is running.
     *
     * @return True if the Nginx service is running, false otherwise.
     */
    public boolean isNginxRunning() {
        return nginxProcessService.isNginxRunning();
    }

    /**
     * Handles the Nginx reload event.
     */
    @EventListener
    public void handleNginxReloadEvent(ApplicationEvent event) {
        reloadNginx();
    }

    /**
     * Reloads the Nginx service.
     */
    public void reloadNginx() {
        nginxProcessService.reloadNginx();
    }
}