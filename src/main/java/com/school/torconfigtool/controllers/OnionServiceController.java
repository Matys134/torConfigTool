package com.school.torconfigtool.controllers;

import com.school.torconfigtool.models.TorConfiguration;
import com.school.torconfigtool.service.TorConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/onion-service")
public class OnionServiceController {

    private final TorConfigurationService torConfigurationService;
    private static final Logger logger = LoggerFactory.getLogger(OnionServiceController.class);

    private static final String NGINX_VHOST_PATH = "/etc/nginx/sites-available/default";

    TorConfiguration torConfiguration = new TorConfiguration();

    @Autowired
    public OnionServiceController(TorConfigurationService torConfigurationService) {
        this.torConfigurationService = torConfigurationService;
    }

    private static final String TORRC_DIRECTORY_PATH = "torrc/onion/";

    @GetMapping
    public String onionServiceConfigurationForm(Model model) {
        List<TorConfiguration> onionConfigs = torConfigurationService.readTorConfigurations("onion");
        Map<String, String> hostnames = new HashMap<>();

        for (TorConfiguration config : onionConfigs) {
            String hostname = readHostnameFile(Integer.parseInt(config.getHiddenServicePort()));
            hostnames.put(config.getHiddenServicePort(), hostname);
        }
        String hostname = readHostnameFile(Integer.parseInt(torConfiguration.getHiddenServicePort())); // Assuming port 80 for this example
        model.addAttribute("hostname", hostname);

        model.addAttribute("onionConfigs", onionConfigs);
        model.addAttribute("hostnames", hostnames);

        return "relay-config"; // The name of the Thymeleaf template to render
    }

    @GetMapping("/current-hostname")
    @ResponseBody
    public String getCurrentHostname() {
        return readHostnameFile(Integer.parseInt(torConfiguration.getHiddenServicePort())); // Or however you determine the correct port
    }


    @PostMapping("/configure")
    public String configureOnionService(@RequestParam int onionServicePort, Model model) {
        try {
            String torrcFilePath = TORRC_DIRECTORY_PATH + "torrc-" + onionServicePort;
            if (!new File(torrcFilePath).exists()) {
                createTorrcFile(torrcFilePath, onionServicePort);
                generateNginxConfig(onionServicePort);
                restartNginx();
            }
            model.addAttribute("successMessage", "Tor Onion Service configured successfully!");
        } catch (IOException e) {
            logger.error("Error configuring Tor Onion Service", e);
            model.addAttribute("errorMessage", "Failed to configure Tor Onion Service.");
        }
        return "relay-config";
    }

    private void generateNginxConfig(int onionServicePort) throws IOException {
        String nginxConfig = buildNginxConfig(onionServicePort);
        editNginxConfig(nginxConfig);
    }

    private String buildNginxConfig(int onionServicePort) {
        // Build the server block
        return String.format("""
                server {
                    listen %d;
                    server_name test;
                    access_log /var/log/nginx/my-website.log;
                    index index.html;
                    root /home/matys/IdeaProjects/torConfigTool/onion/www;
                }
                """, onionServicePort);
    }



    private void editNginxConfig(String nginxConfig) {
        try {
            // Write the nginxConfig to a temporary file
            File tempFile = File.createTempFile("nginx_config", null);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write(nginxConfig);
            }

            // Use sudo to copy the temporary file to the actual nginx configuration file
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "cp", tempFile.getAbsolutePath(), NGINX_VHOST_PATH);
            Process process = processBuilder.start();
            process.waitFor();

            // Clean up the temporary file
            tempFile.delete();
        } catch (IOException | InterruptedException e) {
            logger.error("Error editing Nginx configuration", e);
        }
    }



    private void restartNginx() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "systemctl", "restart", "nginx");
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            logger.error("Error restarting Nginx", e);
        }
    }

    @PostMapping("/start")
    public String startOnionService(Model model) {
        boolean startSuccess = startTorOnionService();
        if (startSuccess) {
            model.addAttribute("successMessage", "Tor Onion Service started successfully!");
        } else {
            model.addAttribute("errorMessage", "Failed to start Tor Onion Service.");
        }
        return "relay-config";
    }

    private boolean startTorOnionService() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "systemctl", "start", "tor");
            Process process = processBuilder.start();

            // Wait for the process to complete
            int exitCode = process.waitFor();

            // Check the exit code to determine if the start was successful
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            logger.error("Error starting Tor Onion Service", e);
            // Log and handle any exceptions that occur during the start
            return false;
        }
    }

    private void createTorrcFile(String filePath, int onionServicePort) throws IOException {
        File torrcFile = new File(filePath);

        // Check if the parent directories exist; if not, attempt to create them
        if (!torrcFile.getParentFile().exists() && !torrcFile.getParentFile().mkdirs()) {
            throw new IOException("Failed to create parent directories for: " + filePath);
        }

        try (BufferedWriter torrcWriter = new BufferedWriter(new FileWriter(torrcFile))) {
            String currentDirectory = System.getProperty("user.dir");
            String hiddenServiceDirs = currentDirectory + "/onion/hiddenServiceDirs";

            File wwwDir = new File(currentDirectory + "/onion/www");
            if (!wwwDir.exists() && !wwwDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + wwwDir.getAbsolutePath());
            }

            File serviceDir = new File(wwwDir, "service-" + onionServicePort);
            if (!serviceDir.exists() && !serviceDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + serviceDir.getAbsolutePath());
            }

            torrcWriter.write("HiddenServiceDir " + hiddenServiceDirs + "/onion-service-" + onionServicePort + "/");
            torrcWriter.newLine();
            torrcWriter.write("HiddenServicePort " + onionServicePort + " 127.0.0.1:" + onionServicePort);

            File indexHtml = new File(serviceDir, "index.html");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(indexHtml))) {
                writer.write("<html><body><h1>Test Onion Service</h1></body></html>");
            }
        }
    }

    private String readHostnameFile(int port) {
        // Adjust the file path as needed
        Path path = Paths.get("onion/hiddenServiceDirs/onion-service-" + port + "/hostname");
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            return "Unable to read hostname file";
        }
    }

    @GetMapping("/upload")
    public String showUploadForm() {
        return "file_upload_form";
    }

    @PostMapping("/upload")
    public String uploadFiles(@RequestParam("files") MultipartFile[] files, Model model) {
        try {
            Arrays.asList(files).stream().forEach(file -> {
                String fileDir = "/home/matys/IdeaProjects/torConfigTool/onion/www/service-80/";
                File outputFile = new File(fileDir + file.getOriginalFilename());

                try(FileOutputStream fos = new FileOutputStream(outputFile)){
                    fos.write(file.getBytes());
                } catch (IOException e) {
                    logger.error("Error during file saving", e);
                    throw new RuntimeException("Error during file saving: "+e.getMessage());
                }
            });

            model.addAttribute("message", "Files uploaded successfully!");
            return "file_upload_form";
        } catch (Exception e) {
            model.addAttribute("message", "Fail! -> uploaded filename: " + Arrays.toString(files));
            return "file_upload_form";
        }
    }
}
