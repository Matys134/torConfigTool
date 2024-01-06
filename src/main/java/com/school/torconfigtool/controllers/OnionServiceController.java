package com.school.torconfigtool.controllers;

import com.school.torconfigtool.RelayUtils;
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
import java.util.*;

@Controller
@RequestMapping("/onion-service")
public class OnionServiceController {

    private static final Logger logger = LoggerFactory.getLogger(OnionServiceController.class);
    private static final String TORRC_DIRECTORY_PATH = "torrc/";
    private final TorConfigurationService torConfigurationService;
    private final List<String> onionServicePorts;
    TorConfiguration torConfiguration = new TorConfiguration();

    @Autowired
    public OnionServiceController(TorConfigurationService torConfigurationService) {
        this.torConfigurationService = torConfigurationService;
        this.onionServicePorts = getAllOnionServicePorts();

        // Set the hiddenServicePort here if it's not being set elsewhere
        if (!onionServicePorts.isEmpty()) {
            torConfiguration.setHiddenServicePort(onionServicePorts.get(0));
        }

        // Check if hiddenServiceDirs directory exists, if not, create it
        String hiddenServiceDirsPath = System.getProperty("user.dir") + "/onion/hiddenServiceDirs";
        File hiddenServiceDirs = new File(hiddenServiceDirsPath);
        if (!hiddenServiceDirs.exists()) {
            boolean dirCreated = hiddenServiceDirs.mkdirs();
            if (!dirCreated) {
                logger.error("Failed to create directory: " + hiddenServiceDirsPath);
            }
        }
    }

    private List<String> getAllOnionServicePorts() {
        List<String> ports = new ArrayList<>();
        File torrcDirectory = new File(TORRC_DIRECTORY_PATH);
        File[] torrcFiles = torrcDirectory.listFiles((dir, name) -> name.endsWith("_onion"));

        if (torrcFiles != null) {
            for (File file : torrcFiles) {
                String fileName = file.getName();
                String port = fileName.substring(fileName.indexOf('-') + 1, fileName.indexOf('_'));
                ports.add(port);
            }
        }

        return ports;
    }

    @GetMapping
    public String onionServiceConfigurationForm(Model model) {
        System.out.println("OnionServiceConfigurationForm called");
        List<TorConfiguration> onionConfigs = torConfigurationService.readTorConfigurations();
        Map<String, String> hostnames = new HashMap<>();

        for (String port : onionServicePorts) {
            String hostname = readHostnameFile(Integer.parseInt(port));
            hostnames.put(port, hostname);
        }
        String hostname = readHostnameFile(Integer.parseInt(torConfiguration.getHiddenServicePort())); // Assuming port 80 for this example
        model.addAttribute("hostname", hostname);

        model.addAttribute("onionConfigs", onionConfigs);
        model.addAttribute("hostnames", hostnames);

        return "relay-config"; // The name of the Thymeleaf template to render
    }


    @GetMapping("/current-hostnames")
    @ResponseBody
    public Map<String, String> getCurrentHostnames() {
        logger.info("Inside getCurrentHostnames method");
        Map<String, String> hostnames = new HashMap<>();
        for (String hiddenServicePortString : onionServicePorts) {
            logger.info("Hidden Service Port: {}", hiddenServicePortString);

            if (hiddenServicePortString != null) {
                String hostname = readHostnameFile(Integer.parseInt(hiddenServicePortString));
                logger.info("Fetched Hostname: {}", hostname);
                hostnames.put(hiddenServicePortString, hostname);
            } else {
                logger.warn("Hidden service port is null");
                hostnames.put(hiddenServicePortString, "Hidden service port is null");
            }
        }
        return hostnames;
    }


    @PostMapping("/configure")
    public String configureOnionService(@RequestParam int onionServicePort, Model model) {
        // Check port availability before configuring the onion service
        if (!RelayUtils.isPortAvailable("torrc-" + onionServicePort + "_onion", onionServicePort)) {
            model.addAttribute("errorMessage", "Port is not available.");
            return "relay-config";
        }

        try {
            String pathToFile = TORRC_DIRECTORY_PATH + "torrc-" + onionServicePort + "_onion";
            if (!new File(pathToFile).exists()) {
                createTorrcFile(pathToFile, onionServicePort);
                generateNginxConfig(onionServicePort);
                restartNginx();
            }
            torConfiguration.setHiddenServicePort(String.valueOf(onionServicePort));
            logger.info("Hidden Service Port set to: {}", onionServicePort);
            model.addAttribute("successMessage", "Tor Onion Service configured successfully!");
        } catch (IOException e) {
            logger.error("Error configuring Tor Onion Service", e);
            model.addAttribute("errorMessage", "Failed to configure Tor Onion Service.");
        }
        return "relay-config";
    }

    private void generateNginxConfig(int onionServicePort) throws IOException {
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
            torrcWriter.write("HiddenServicePort 80 127.0.0.1:" + onionServicePort);

            File indexHtml = new File(serviceDir, "index.html");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(indexHtml))) {
                writer.write("<html><body><h1>Test Onion Service</h1></body></html>");
            }
        }
    }


    private String readHostnameFile(int port) {
        // Get the current working directory
        String currentDirectory = System.getProperty("user.dir");

        // Build the correct path to the hostname file
        Path path = Paths.get(currentDirectory, "onion", "hiddenServiceDirs", "onion-service-" + port, "hostname");
        System.out.println(path);
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            return "Unable to read hostname file";
        }
    }

    @GetMapping("/upload/{port}")
    public String showUploadForm(@PathVariable("port") int port, Model model) {
        List<String> fileNames = getUploadedFiles(port);
        model.addAttribute("uploadedFiles", fileNames);
        return "file_upload_form";
    }

    @PostMapping("/remove-file/{fileName}/{port}")
    public String removeFile(@PathVariable("fileName") String fileName, @PathVariable("port") int port, Model model) {
        try {
            String fileDir = "onion/www/service-" + port + "/";
            File fileToRemove = new File(fileDir + fileName);

            if (fileToRemove.exists()) {
                if (!fileToRemove.delete()) {
                    model.addAttribute("message", "Error deleting the file.");
                } else {
                    model.addAttribute("message", "File deleted successfully.");
                }
            } else {
                model.addAttribute("message", "File doesn't exist.");
            }
        } catch (Exception e) {
            model.addAttribute("message", "Error: " + e.getMessage());
        }

        List<String> fileNames = getUploadedFiles(port);
        model.addAttribute("uploadedFiles", fileNames);

        return "file_upload_form";
    }

    @PostMapping("/upload/{port}")
    public String uploadFiles(@RequestParam("files") MultipartFile[] files, @PathVariable("port") int port, Model model) {
        try {
            Arrays.stream(files).forEach(file -> {
                String fileDir = "onion/www/service-" + port + "/";
                File outputFile = new File(fileDir + file.getOriginalFilename());

                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(file.getBytes());
                } catch (IOException e) {
                    logger.error("Error during file saving", e);
                    throw new RuntimeException("Error during file saving: " + e.getMessage());
                }
            });

            List<String> fileNames = getUploadedFiles(port);
            model.addAttribute("uploadedFiles", fileNames);

            model.addAttribute("message", "Files uploaded successfully!");
            return "file_upload_form";
        } catch (Exception e) {
            model.addAttribute("message", "Fail! -> uploaded filename: " + Arrays.toString(files));
            return "file_upload_form";
        }
    }

    private List<String> getUploadedFiles(int port) {
        String uploadDir = "onion/www/service-" + port + "/";
        File folder = new File(uploadDir);
        return Arrays.asList(Objects.requireNonNull(folder.list()));
    }

    @GetMapping("/edit")
    public String editOnionService(Model model) {
        List<TorConfiguration> onionConfigs = torConfigurationService.readTorConfigurations();
        model.addAttribute("onionConfigs", onionConfigs);
        return "edit-onion-service";
    }
}
