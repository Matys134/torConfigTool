package com.school.torconfigtool.nginx.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service class manages the configuration of Nginx.
 * It uses an instance of NginxConfigWriter to perform the actual configuration changes.
 */
@Service
public class NginxConfigManager {

    private final NginxConfigWriter nginxConfigWriter;

    /**
     * Constructs a new NginxConfigManager with the given NginxConfigWriter.
     * @param nginxConfigWriter the NginxConfigWriter to use for writing the configuration
     */
    @Autowired
    public NginxConfigManager(NginxConfigWriter nginxConfigWriter) {
        this.nginxConfigWriter = nginxConfigWriter;
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
     */
    public void revertNginxDefaultConfig() {
        String rootDirectory = "/home/matys/git/torConfigTool/onion/www/service-80";
        nginxConfigWriter.writeCommonConfig(rootDirectory);
    }

    /**
     * Modifies the default Nginx configuration with the given parameters.
     * @param programLocation the location of the program
     * @param randomString a random string
     * @param webTunnelUrl the URL of the web tunnel
     */
    public void modifyNginxDefaultConfig(String programLocation, String randomString, String webTunnelUrl) {
        nginxConfigWriter.writeModifiedConfig(programLocation, randomString, webTunnelUrl);
    }
}