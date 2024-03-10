package com.school.torconfigtool.service;

import com.school.torconfigtool.model.TorConfig;
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
        // Create a new process builder
        ProcessBuilder processBuilder = new ProcessBuilder();

        // Set the command for the process builder
        processBuilder.command("bash", "-c", "sudo systemctl start nginx");

        try {
            // Start the process and wait for it to finish
            Process process = processBuilder.start();
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is used to reload the Nginx server.
     * It constructs a command to reload the server and then executes it.
     * If the command execution fails, it logs the error.
     */
    public void reloadNginx() {
        // Print the action to the console
        System.out.println("Reloading Nginx");

        // Create a new process builder
        ProcessBuilder processBuilder = new ProcessBuilder();

        // Set the command for the process builder
        processBuilder.command("bash", "-c", "sudo systemctl reload nginx");

        try {
            // Start the process and wait for it to finish
            Process process = processBuilder.start();
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is used to generate the Nginx configuration.
     * It creates a new index.html file in the appropriate directory.
     * If the file creation fails, it logs the error.
     */
    public void generateNginxConfig() {
        try {
            // Get the current working directory
            String currentDirectory = System.getProperty("user.dir");

            // Get the index.html file
            File indexHtml = getFile(currentDirectory);

            // Write the HTML content to the file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(indexHtml))) {
                writer.write("<html><body><h1>Test Onion Service</h1></body></html>");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is used to get the index.html file.
     * It creates the necessary directories and the file if they do not exist.
     *
     * @param currentDirectory The current working directory.
     * @return The index.html file.
     * @throws IOException If an I/O error occurs.
     */
    private File getFile(String currentDirectory) throws IOException {
        // Create the www directory if it does not exist
        File wwwDir = new File(currentDirectory + "/onion/www");
        if (!wwwDir.exists() && !wwwDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + wwwDir.getAbsolutePath());
        }

        // Create the service directory if it does not exist
        File serviceDir = new File(wwwDir, "service-" + 80);
        if (!serviceDir.exists() && !serviceDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + serviceDir.getAbsolutePath());
        }

        // Return the index.html file
        return new File(serviceDir, "index.html");
    }

    /**
     * This method is used to change the root directory in the Nginx configuration.
     * It reads the configuration file, modifies the root directory line, and then writes the file back.
     * If the file reading or writing fails, it logs the error.
     *
     * @param rootDirectory The new root directory.
     */
    public void changeRootDirectory(String rootDirectory) {
        // The path to the default configuration file
        Path defaultConfigPath = Paths.get("/etc/nginx/sites-available/default");

        try {
            // Read the file into a list of strings
            List<String> lines = Files.readAllLines(defaultConfigPath);

            // Clear the list and add the new configuration lines
            lines.clear();
            lines.add("server {");
            lines.add("    listen 80 default_server;");
            lines.add("    listen [::]:80 default_server;");
            lines.add("    root " + rootDirectory + ";");
            lines.add("    index index.html index.htm index.nginx-debian.html;");
            lines.add("    server_name _;");
            lines.add("    location / {");
            lines.add("        try_files $uri $uri/ =404;");
            lines.add("    }");
            lines.add("}");

            // Write the list back to the file
            Files.write(defaultConfigPath, lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Create a new process builder
        ProcessBuilder processBuilder = new ProcessBuilder();

        // Set the command for the process builder
        processBuilder.command("bash", "-c", "sudo systemctl reload nginx");
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
            List<String> lines = getDefaultNginxConfigLines();

            // Write the list to the file
            Files.write(defaultConfigPath, lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is used to get the default Nginx configuration lines.
     *
     * @return The default Nginx configuration lines.
     */
    public List<String> getDefaultNginxConfigLines() {
        String currentDirectory = System.getProperty("user.dir");
        return generateNginxConfigLines(currentDirectory + "/onion/www/service-80", "_", "", "");
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
    public void modifyNginxDefaultConfig(String programLocation, String randomString, String webTunnelUrl) {
        // The path to the default configuration file
        Path defaultConfigPath = Paths.get("/etc/nginx/sites-available/default");

        try {
            // Clear the file and write the initial configuration
            List<String> lines = generateNginxConfigLines(programLocation + "/onion/www/service-80", "$SERVER_ADDRESS",
                    programLocation + "/onion/certs/service-80/fullchain.pem",
                    programLocation + "/onion/certs/service-80/key.pem");

            // Write the list to the file
            Files.write(defaultConfigPath, lines);

            // Issue and install the certificates
            acmeService.installCert(webTunnelUrl);

            // Add the rest of the configuration lines...
            lines.add("    location = /" + randomString + " {");
            lines.add("        proxy_pass http://127.0.0.1:15000;");
            lines.add("        proxy_http_version 1.1;");
            lines.add("        proxy_set_header Upgrade $http_upgrade;");
            lines.add("        proxy_set_header Connection \"upgrade\";");
            lines.add("        proxy_set_header Accept-Encoding \"\";");
            lines.add("        proxy_set_header Host $host;");
            lines.add("        proxy_set_header X-Real-IP $remote_addr;");
            lines.add("        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;");
            lines.add("        proxy_set_header X-Forwarded-Proto $scheme;");
            lines.add("        add_header Front-End-Https on;");
            lines.add("        proxy_redirect off;");
            lines.add("        access_log off;");
            lines.add("        error_log off;");
            lines.add("    }");
            lines.add("}");

            // Write the list back to the file
            Files.write(defaultConfigPath, lines);
        }  catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
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
            // Return false if an exception occurs
            return false;
        }
    }

    public void generateNginxConfig(int onionServicePort) {
        String nginxConfig = buildNginxConfig(onionServicePort);
        editNginxConfig(nginxConfig, onionServicePort);
    }

    private String buildNginxConfig(int onionServicePort) {

        String currentDirectory = System.getProperty("user.dir");
        // Build the server block
        return String.format("""
                server {
                    listen 127.0.0.1:%d;
                    server_name torrc-test_guard;
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

            if (!tempFile.delete()) {
                throw new IOException("Failed to delete temporary file: " + tempFile.getAbsolutePath());
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
    }

    public List<String> getAllServices() {
        List<String> allServices = new ArrayList<>();
        // Get the list of all onion services
        List<TorConfig> onionConfigs = torConfigService.readTorConfigurationsFromFolder(torConfigService.buildFolderPath(), "onion");
        for (TorConfig config : onionConfigs) {
            allServices.add(config.getHiddenServicePort());
        }

        // Get the list of all webTunnels
        List<TorConfig> bridgeConfigs = torConfigService.readTorConfigurationsFromFolder(torConfigService.buildFolderPath(), "bridge");
        for (TorConfig config : bridgeConfigs) {
            allServices.add(config.getBridgeConfig().getNickname());
        }
        return allServices;
    }

    private List<String> generateNginxConfigLines(String rootDirectory, String serverName, String certPath, String certKey) {
        List<String> lines = new ArrayList<>();
        lines.add("server {");
        lines.add("    listen 80 default_server;");
        lines.add("    listen [::]:80 default_server;");
        lines.add("    root " + rootDirectory + ";");
        lines.add("    index index.html index.htm index.nginx-debian.html;");
        lines.add("    server_name " + serverName + ";");
        lines.add("    ssl_certificate " + certPath + ";");
        lines.add("    ssl_certificate_key " + certKey + ";");
        lines.add("    location / {");
        lines.add("        try_files $uri $uri/ =404;");
        lines.add("    }");
        lines.add("}");
        return lines;
    }
}