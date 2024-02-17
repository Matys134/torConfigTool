package com.school.torconfigtool;

import com.school.torconfigtool.nginx.service.NginxConfigGenerator;
import com.school.torconfigtool.nginx.service.NginxConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Service class for managing Nginx configurations.
 * This class provides methods to generate, modify and revert Nginx configurations.
 */
@Service
public class NginxConfigService {

    private final NginxConfigGenerator nginxConfigGenerator;
    private final NginxConfigManager nginxConfigManager;
    private static final Logger logger = LoggerFactory.getLogger(NginxConfigService.class);

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

    public void generateNginxConfig(int onionServicePort) throws IOException {
        String nginxConfig = buildNginxConfig(onionServicePort);
        editNginxConfig(nginxConfig, onionServicePort);
    }

    private String buildNginxConfig(int onionServicePort) {

        String currentDirectory = System.getProperty("user.dir");
        // Build the server block
        return String.format("""
                server {
                    listen 127.0.0.1:%d;
                    server_name test;
                    access_log /var/log/nginx/my-website.log;
                    index index.html;
                    root %s/onion/www/service-%d;
                }
                """, onionServicePort, currentDirectory, onionServicePort);
    }

    private void editNginxConfig(String nginxConfig, int onionServicePort) {
        try {
            // Write the nginxConfig to a temporary file
            File tempFile = File.createTempFile("nginx_config", null);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write(nginxConfig);
            }

            String onionServiceConfigPath = "/etc/nginx/sites-available/onion-service-" + onionServicePort;

            // Use sudo to copy the temporary file to the actual nginx configuration file
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "cp", tempFile.getAbsolutePath(), onionServiceConfigPath);
            Process process = processBuilder.start();
            process.waitFor();

            // Create a symbolic link to the nginx configuration file
            String enableConfigPath = "/etc/nginx/sites-enabled/onion-service-" + onionServicePort;
            processBuilder = new ProcessBuilder("sudo", "ln", "-s", onionServiceConfigPath, enableConfigPath);
            process = processBuilder.start();
            process.waitFor();

            // Clean up the temporary file
            boolean isDeleted = tempFile.delete();

            if (!isDeleted) {
                logger.error("Failed to delete temporary file: " + tempFile);
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error editing Nginx configuration", e);
        }
    }
}