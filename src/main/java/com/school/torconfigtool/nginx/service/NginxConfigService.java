package com.school.torconfigtool.nginx.service;

import com.school.torconfigtool.NginxConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class for managing Nginx configurations.
 * This class provides methods to generate, modify and revert Nginx configurations.
 */
@Service
public class NginxConfigService {

    private final NginxConfigGenerator nginxConfigGenerator;
    private final NginxConfigManager nginxConfigManager;

    /**
     * Constructor for NginxConfigService.
     * @param nginxConfigGenerator the NginxConfigGenerator to be used for generating Nginx configurations.
     * @param nginxConfigManager the NginxConfigManager to be used for managing Nginx configurations.
     */
    @Autowired
    public NginxConfigService(NginxConfigGenerator nginxConfigGenerator, NginxConfigManager nginxConfigManager) {
        this.nginxConfigGenerator = nginxConfigGenerator;
        this.nginxConfigManager = nginxConfigManager;
    }

    /**
     * Generates a new Nginx configuration.
     */
    public void generateNginxConfig() {
        nginxConfigGenerator.generateNginxConfig();
    }

    /**
     * Changes the root directory of the Nginx configuration.
     * @param rootDirectory the new root directory.
     */
    public void changeRootDirectory(String rootDirectory) {
        nginxConfigManager.changeRootDirectory(rootDirectory);
    }

    /**
     * Reverts the Nginx configuration to its default state.
     */
    public void revertNginxDefaultConfig() {
        nginxConfigManager.revertNginxDefaultConfig();
    }

    /**
     * Modifies the default Nginx configuration.
     * @param programLocation the location of the program.
     * @param randomString a random string.
     * @param webTunnelUrl the URL of the web tunnel.
     */
    public void modifyNginxDefaultConfig(String programLocation, String randomString, String webTunnelUrl) {
        nginxConfigManager.modifyNginxDefaultConfig(programLocation, randomString, webTunnelUrl);
    }
}