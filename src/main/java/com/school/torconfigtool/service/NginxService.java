package com.school.torconfigtool.service;

import com.school.torconfigtool.model.TorConfig;
import com.school.torconfigtool.util.Constants;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    public void configureNginxForOnionService(int onionServicePort) throws IOException {
        String nginxConfig = buildNginxServerBlock(onionServicePort);
        deployOnionServiceNginxConfig(nginxConfig, onionServicePort);
        createIndexFile(onionServicePort, System.getProperty("user.dir"));
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
     * This method is used to revert the Nginx configuration to its default state.
     * It clears the configuration file and writes the initial configuration.
     * If the file writing fails, it logs the error.
     */
    public void revertNginxDefaultConfig() {
        // The path to the default configuration file
        Path defaultConfigPath = Paths.get("/etc/nginx/sites-available/default");

        try {
            // Clear the file and write the initial configuration
            List<String> lines = getDefaultNginxConfigLines(80);

            // Write the list to the file
            Files.write(defaultConfigPath, lines);
        } catch (IOException e) {
            throw new RuntimeException("Failed to revert Nginx default configuration", e);
        }
    }

    /**
     * This method is used to get the default Nginx configuration lines.
     *
     * @return The default Nginx configuration lines.
     */
    private static List<String> getDefaultNginxConfigLines(int webtunnelPort) {
        String currentDirectory = System.getProperty("user.dir");
        List<String> lines = new ArrayList<>();
        lines.add("server {");
        lines.add("    listen 80 default_server;");
        lines.add("    listen [::]:80 default_server;");
        lines.add("    root " + currentDirectory + "/onion/www/service-" + webtunnelPort + ";");
        lines.add("    index index.html index.htm index.nginx-debian.html;");
        lines.add("    server_name _;");
        lines.add("    location / {");
        lines.add("        try_files $uri $uri/ =404;");
        lines.add("    }");
        lines.add("}");
        return lines;
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
    public void modifyNginxDefaultConfig(String programLocation, String randomString, String webTunnelUrl, int webtunnelPort, int transportListenAddr) throws Exception {
        // Build the new configuration
        StringBuilder sb = new StringBuilder();
        sb.append("server {\n");
        sb.append("    listen [::]:").append(webtunnelPort).append(" ssl http2;\n");
        sb.append("    listen ").append(webtunnelPort).append(" ssl http2;\n");
        sb.append("    root ").append(programLocation).append("/onion/www/service-").append(webtunnelPort).append(";\n");
        sb.append("    index index.html index.htm index.nginx-debian.html;\n");
        sb.append("    server_name $SERVER_ADDRESS;\n");
        sb.append("    ssl_certificate ").append(programLocation).append("/onion/certs/service-").append(webtunnelPort).append("/fullchain.pem;\n");
        sb.append("    ssl_certificate_key ").append(programLocation).append("/onion/certs/service-").append(webtunnelPort).append("/key.pem;\n");
        sb.append("    location = /").append(randomString).append(" {\n");
        sb.append("        proxy_pass http://127.0.0.1:").append(transportListenAddr).append(";\n");
        sb.append("        proxy_http_version 1.1;\n");
        sb.append("        proxy_set_header Upgrade $http_upgrade;\n");
        sb.append("        proxy_set_header Connection \"upgrade\";\n");
        sb.append("        proxy_set_header Accept-Encoding \"\";\n");
        sb.append("        proxy_set_header Host $host;\n");
        sb.append("        proxy_set_header X-Real-IP $remote_addr;\n");
        sb.append("        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n");
        sb.append("        proxy_set_header X-Forwarded-Proto $scheme;\n");
        sb.append("        add_header Front-End-Https on;\n");
        sb.append("        proxy_redirect off;\n");
        sb.append("        access_log off;\n");
        sb.append("        error_log off;\n");
        sb.append("    }\n");
        sb.append("}\n");

        // Issue and install the certificates
        acmeService.installCert(webTunnelUrl, webtunnelPort);

        // Deploy the configuration
        deployOnionServiceNginxConfig(sb.toString(), webtunnelPort);
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

    private String buildNginxServerBlock(int onionServicePort) {

        String currentDirectory = System.getProperty("user.dir");
        // Build the server block
        return String.format("""
                server {
                    listen 127.0.0.1:%d;
                    access_log /var/log/nginx/my-website.log;
                    index index.html;
                    root %s/onion/www/service-%d;
                }
                """, onionServicePort, currentDirectory, onionServicePort);
    }


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
}