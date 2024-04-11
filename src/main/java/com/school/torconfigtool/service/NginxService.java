package com.school.torconfigtool.service;

import com.school.torconfigtool.model.TorConfig;
import com.school.torconfigtool.util.Constants;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * NginxService is a service class responsible for managing the Nginx server.
 */
@Service
public class NginxService {

    // TorConfigurationService instance for managing Tor configurations
    private final TorConfigService torConfigService;
    private final AcmeService acmeService;

    /**
     * Constructor for the NginxService class.
     * It initializes the TorConfigurationService instance.
     *
     * @param torConfigService The TorConfigurationService instance.
     */
    public NginxService(TorConfigService torConfigService, AcmeService acmeService) {
        this.torConfigService = torConfigService;
        this.acmeService = acmeService;
    }

    /**
     * This method is used to start the Nginx server.
     * It constructs a command to start the server and then executes it.
     * If the command execution fails, it logs the error.
     */
    public void startNginx() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "sudo systemctl start nginx");

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IOException("Failed to start Nginx");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to start Nginx", e);
        }
    }

    /**
     * This method is used to reload the Nginx server.
     * It constructs a command to reload the server and then executes it.
     * If the command execution fails, it logs the error.
     */
    public void reloadNginx() {
        // Check if Nginx is running
        if (!isNginxRunning()) {
            // If Nginx is not running, start it
            startNginx();
        } else {
            // If Nginx is running, reload it
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", "sudo systemctl reload nginx");

            try {
                Process process = processBuilder.start();
                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    throw new IOException("Failed to reload Nginx");
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Failed to reload Nginx", e);
            }
        }
    }

    /**
     * This method is used to generate the Nginx configuration.
     * It creates the necessary directories and the index.html file.
     * If the file writing fails, it logs the error.
     */
    public void configureNginx(int servicePort) throws IOException {
        String nginxConfig = buildNginxServerBlock(servicePort);
        deployOnionServiceNginxConfig(nginxConfig, servicePort);
        createIndexFile(servicePort, System.getProperty("user.dir"));
    }

    public void createIndexFile(int onionServicePort, String currentDirectory) throws IOException {
        File wwwDir = new File(currentDirectory + "/onion/www");
        if (!wwwDir.exists() && !wwwDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + wwwDir.getAbsolutePath());
        }

        File serviceDir = new File(wwwDir, "service-" + onionServicePort);
        if (!serviceDir.exists() && !serviceDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + serviceDir.getAbsolutePath());
        }

        File indexHtml = new File(serviceDir, "index.html");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(indexHtml))) {
            writer.write("<html><body><h1>Onion Service</h1></body></html>");
        }
    }

    /**
     * This method is used to modify the Nginx configuration.
     * It clears the configuration file, writes the initial configuration, installs the certificates, and then writes the new configuration.
     * If the file reading or writing fails, it logs the error.
     *
     * @param programLocation The current working directory.
     * @param randomString A random string used in the configuration.
     * @param webTunnelUrl The URL of the web tunnel where the certificate will be installed.
     */
    public void configureNginxServerForWebtunnel(String programLocation, String randomString, String webTunnelUrl, int webtunnelPort, int transportListenAddr) throws Exception {
        // Build the initial configuration with port 80
        String initialNginxConfig = buildNginxServerBlock(80);
        deployOnionServiceNginxConfig(initialNginxConfig, webtunnelPort);
        createIndexFile(webtunnelPort, System.getProperty("user.dir"));

        // Start the Nginx server
        startNginx();

        // Issue and install the certificates
        acmeService.installCert(webTunnelUrl, webtunnelPort);

        // Stop the Nginx server
        stopNginx();

        // Build the new configuration with port 443
        String finalNginxConfig = String.format("""
            server {
                listen [::]:443 ssl http2;
                listen 443 ssl http2;
                root %s/onion/www/service-%d;
                index index.html index.htm index.nginx-debian.html;
                server_name $SERVER_ADDRESS;
                ssl_certificate %s/onion/certs/service-%d/fullchain.pem;
                ssl_certificate_key %s/onion/certs/service-%d/key.pem;
                location = /%s {
                    proxy_pass http://127.0.0.1:%d;
                    proxy_http_version 1.1;
                    proxy_set_header Upgrade $http_upgrade;
                    proxy_set_header Connection "upgrade";
                    proxy_set_header Accept-Encoding "";
                    proxy_set_header Host $host;
                    proxy_set_header X-Real-IP $remote_addr;
                    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                    proxy_set_header X-Forwarded-Proto $scheme;
                    add_header Front-End-Https on;
                    proxy_redirect off;
                    access_log off;
                    error_log off;
                }
            }
            """, programLocation, webtunnelPort, programLocation, webtunnelPort, programLocation, webtunnelPort,
                randomString, transportListenAddr);

        // Deploy the final configuration
        deployOnionServiceNginxConfig(finalNginxConfig, webtunnelPort);

        // Start the Nginx server again
        startNginx();
    }

    /**
     * This method is used to check if the Nginx server is running.
     * It constructs a command to check the server status and then executes it.
     * If the command execution fails, it logs the error.
     *
     * @return True if the server is running, false otherwise.
     */
    public boolean isNginxRunning() {
        // Create a new process builder
        ProcessBuilder processBuilder = new ProcessBuilder();

        // Set the command for the process builder
        processBuilder.command("bash", "-c", "systemctl is-active nginx");

        try {
            // Start the process and wait for it to finish
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            // Return true if the exit code is 0, false otherwise
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    /**
     * Builds the Nginx server block configuration.
     *
     * @param onionServicePort The port number for the Onion Service.
     * @return The Nginx server block configuration.
     */
    private String buildNginxServerBlock(int onionServicePort) {
        String currentDirectory = System.getProperty("user.dir");
        // Build the server block
        return String.format("""
            server {
                listen %d;
                access_log /var/log/nginx/my-website.log;
                index index.html;
                root %s/onion/www/service-%d;
            }
            """, onionServicePort, currentDirectory, onionServicePort);
    }


    /**
     * Deploys the Nginx configuration for the Onion Service.
     *
     * @param nginxConfig The Nginx configuration to be deployed.
     * @param onionServicePort The port number for the Onion Service.
     * @throws RuntimeException If there is an error while editing the Nginx configuration.
     */
    private void deployOnionServiceNginxConfig(String nginxConfig, int onionServicePort) {
        try {
            // Write the nginxConfig to a temporary file
            File tempFile = File.createTempFile("nginx_config", null);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write(nginxConfig);
            }

            String onionServiceConfigPath = "/etc/nginx/sites-available/onion-service-" + onionServicePort;

            // Use sudo to copy the temporary file to the actual nginx configuration file
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "cp", tempFile.getAbsolutePath(),
                    onionServiceConfigPath);
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
                throw new IOException("Failed to delete temporary file");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to edit Nginx configuration", e);
        }
    }

    /**
     * Stops the Nginx server.
     *
     * @throws RuntimeException If there is an error while stopping the Nginx server.
     */
    public void stopNginx() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "systemctl", "stop", "nginx");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Failed to stop Nginx");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to stop Nginx", e);
        }
    }

    /**
     * Gets all Onion and Web Tunnel services.
     *
     * @return A list of all Onion and Web Tunnel services.
     */
    public List<String> getAllOnionAndWebTunnelServices() {
        List<String> allServices = new ArrayList<>();
        // Get the list of all onion services
        List<TorConfig> onionConfigs = torConfigService.readTorConfigurations(Constants.TORRC_DIRECTORY_PATH,
                "onion");
        for (TorConfig config : onionConfigs) {
            allServices.add(config.getOnionConfig().getHiddenServicePort());
        }

        // Get the list of all webTunnels
        List<TorConfig> bridgeConfigs = torConfigService.readTorConfigurations(Constants.TORRC_DIRECTORY_PATH,
                "bridge");
        for (TorConfig config : bridgeConfigs) {
            allServices.add(config.getBridgeConfig().getNickname());
        }
        return allServices;
    }

    /**
     * Updates the Nginx configuration.
     *
     * @param newPort The new port number.
     * @param webtunnelPort The port number for the Web Tunnel.
     * @throws RuntimeException If there is an error while updating the Nginx configuration.
     */
    public void updateNginxConfig(int newPort, int webtunnelPort) {
        String configPath = "/etc/nginx/sites-available/onion-service-" + webtunnelPort;
        String sedCommand = String.format("s/proxy_pass http:\\/\\/127.0.0.1:[0-9]*/proxy_pass http:\\/\\/127.0.0.1:%d/g", newPort);

        ProcessBuilder processBuilder = new ProcessBuilder("sudo", "sed", "-i", sedCommand, configPath);
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Failed to update Nginx configuration");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to update Nginx configuration", e);
        }
    }
}