package com.school.torconfigtool.controllers;

import com.simtechdata.waifupnp.UPnP;
import com.school.torconfigtool.RelayOperationException;
import com.school.torconfigtool.models.TorConfiguration;
import com.school.torconfigtool.service.ProcessManagementService;
import com.school.torconfigtool.service.TorConfigurationService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

@Controller
@RequestMapping("/relay-operations")
public class RelayOperationsController {

    private static final Logger logger = LoggerFactory.getLogger(RelayOperationsController.class);
    private final TorConfigurationService torConfigurationService;
    private final ProcessManagementService processManagementService;

    public RelayOperationsController(TorConfigurationService torConfigurationService,
                                     ProcessManagementService processManagementService) {
        this.torConfigurationService = torConfigurationService;
        this.processManagementService = processManagementService;

        try {
            Path dataDirectoryPath = Paths.get(System.getProperty("user.dir"), "torrc", "dataDirectory");
            if (!dataDirectoryPath.toFile().exists()) {
                Files.createDirectory(dataDirectoryPath);
            }
        } catch (IOException e) {
            logger.error("Failed to create dataDirectory folder", e);
        }
    }

    @GetMapping
    public String relayOperations(Model model) {
        System.out.println("Inside relayOperations method");
        String folderPath = torConfigurationService.buildFolderPath();
        model.addAttribute("guardConfigs", torConfigurationService.readTorConfigurationsFromFolder(folderPath, "guard"));

        model.addAttribute("bridgeConfigs", torConfigurationService.readTorConfigurationsFromFolder(folderPath, "bridge"));

        model.addAttribute("onionConfigs", torConfigurationService.readTorConfigurationsFromFolder(folderPath, "onion"));
        List<TorConfiguration> onionConfigs = torConfigurationService.readTorConfigurationsFromFolder(folderPath, "onion");

        logger.info("OnionConfigs: {}", onionConfigs);

        // Create a map to store hostnames for onion services
        Map<String, String> hostnames = new HashMap<>();
        for (TorConfiguration config : onionConfigs) {
            String hostname = readHostnameFile(config.getHiddenServicePort());
            hostnames.put(config.getHiddenServicePort(), hostname);
            logger.info("Hostname for port {}: {}", config.getHiddenServicePort(), hostname);
        }

        List<TorConfiguration> bridgeConfigs = torConfigurationService.readTorConfigurationsFromFolder(folderPath, "bridge");
        Map<String, String> webtunnelLinks = new HashMap<>();
        for (TorConfiguration config : bridgeConfigs) {
            String webtunnelLink = getWebtunnelLink(config.getBridgeRelayConfig().getNickname());
            webtunnelLinks.put(config.getBridgeRelayConfig().getNickname(), webtunnelLink);
        }
        model.addAttribute("webtunnelLinks", webtunnelLinks);

        logger.info("Hostnames: {}", hostnames);
        model.addAttribute("hostnames", hostnames);

        return "relay-operations";
    }

    private String readHostnameFile(String hiddenServicePort) {
        // The base directory where your hidden services directories are stored
        String hiddenServiceBaseDir = Paths.get(System.getProperty("user.dir"), "onion", "hiddenServiceDirs").toString();
        Path hostnameFilePath = Paths.get(hiddenServiceBaseDir, "onion-service-" + hiddenServicePort, "hostname");

        try {
            // Read all the lines in the hostname file and return the first line which should be the hostname
            List<String> lines = Files.readAllLines(hostnameFilePath);
            return lines.isEmpty() ? "No hostname found" : lines.getFirst();
        } catch (IOException e) {
            logger.error("Unable to read hostname file for port {}: {}", hiddenServicePort, e.getMessage());
            return "Unable to read hostname file";
        }
    }


    @PostMapping("/stop")
    public String stopRelay(@RequestParam String relayNickname, @RequestParam String relayType, Model model) {
        String view = changeRelayState(relayNickname, relayType, model, false);

        new Thread(() -> {
            try {
                waitForStatusChange(relayNickname, relayType, "offline");
            } catch (InterruptedException e) {
                logger.error("Error while waiting for relay to stop", e);
            }
        }).start();

        return view;
    }

    private void waitForStatusChange(String relayNickname, String relayType, String expectedStatus) throws InterruptedException {
        System.out.println("Waiting for status change");
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 30000) { // 30 seconds timeout
            String status = getRelayStatus(relayNickname, relayType);
            System.out.println("Status: " + status);
            if (expectedStatus.equals(status)) {
                checkAndManageNginxStatus();
                break;
            }
            Thread.sleep(500); // wait for 500 milliseconds before the next check
        }
    }

    @PostMapping("/start")
    public String startRelay(@RequestParam String relayNickname, @RequestParam String relayType, Model model) {
        openOrPort(relayNickname, relayType);
        String view = changeRelayState(relayNickname, relayType, model, true);

        FutureTask<String> futureTask = new FutureTask<>(() -> {
            try {
                waitForStatusChange(relayNickname, relayType, "online");
            } catch (InterruptedException e) {
                logger.error("Error while waiting for relay to start", e);
            }
            return "Task Completed";
        });

        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(futureTask);

        try {
            // This will make the current thread to wait until the task is completed
            String result = futureTask.get();
            logger.info("Result from futureTask: {}", result);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error while waiting for futureTask to complete", e);
        }

        executor.shutdown(); // Always remember to shutdown executor

        return view;
    }

    @GetMapping("/status")
    @ResponseBody
    public String getRelayStatus(@RequestParam String relayNickname, @RequestParam String relayType) {
        String torrcFilePath = buildTorrcFilePath(relayNickname, relayType).toString();
        int pid = processManagementService.getTorRelayPID(torrcFilePath);
        return pid > 0 ? "online" : (pid == -1 ? "offline" : "error");
    }

    private String changeRelayState(String relayNickname, String relayType, Model model, boolean start) {
        Path torrcFilePath = buildTorrcFilePath(relayNickname, relayType);
        String operation = start ? "start" : "stop";
        try {
            processRelayOperation(torrcFilePath, relayNickname, start);
            model.addAttribute("successMessage", "Tor Relay " + operation + "ed successfully!");
        } catch (RelayOperationException | IOException | InterruptedException e) {
            logger.error("Failed to {} Tor Relay for relayNickname: {}", operation, relayNickname, e);
            model.addAttribute("errorMessage", "Failed to " + operation + " Tor Relay.");
        }
        return relayOperations(model);
    }

    private void processRelayOperation(Path torrcFilePath, String relayNickname, boolean start) throws IOException, InterruptedException {
        if (!torrcFilePath.toFile().exists()) {
            throw new RelayOperationException("Torrc file does not exist for relay: " + relayNickname);
        }
        if (start) {
            // Step 1: Retrieve Fingerprints
            List<String> allFingerprints = getAllRelayFingerprints();

            // Step 2: Update the torrc File with fingerprints
            updateTorrcWithFingerprints(torrcFilePath, allFingerprints);

            // Step 3: Start the Relay
            String command = "tor -f " + torrcFilePath.toAbsolutePath();
            System.out.println("Executing command: " + command);
            int exitCode = processManagementService.executeCommand(command);
            if (exitCode != 0) {
                throw new RelayOperationException("Failed to start Tor Relay service.");
            }


        } else {
            int pid = processManagementService.getTorRelayPID(torrcFilePath.toString());
            if (pid > 0) {
                String command = "kill -SIGINT " + pid;
                int exitCode = processManagementService.executeCommand(command);
                if (exitCode != 0) {
                    throw new RelayOperationException("Failed to stop Tor Relay service.");
                }
            } else if (pid == -1) {
                throw new RelayOperationException("Tor Relay is not running.");
            } else {
                throw new RelayOperationException("Error occurred while retrieving PID for Tor Relay.");
            }
        }
    }


    private Path buildTorrcFilePath(String relayNickname, String relayType) {
        return Paths.get(System.getProperty("user.dir"), "torrc", "torrc-" + relayNickname + "_" + relayType);
    }

    // This method could be moved from RelayController to here
    private List<String> getFingerprints(String dataDirectoryPath) {
        // Assuming the dataDirectoryPath is something like "torrc/dataDirectory"
        List<String> fingerprints = new ArrayList<>();
        File dataDirectory = new File(dataDirectoryPath);
        File[] dataDirectoryFiles = dataDirectory.listFiles(File::isDirectory);

        if (dataDirectoryFiles != null) {
            for (File dataDir : dataDirectoryFiles) {
                String fingerprintFilePath = dataDir.getAbsolutePath() + File.separator + "fingerprint";
                String fingerprint = readFingerprint(fingerprintFilePath);
                if (fingerprint != null) {
                    fingerprints.add(fingerprint);
                }
            }
        }
        return fingerprints;
    }

    private String readFingerprint(String fingerprintFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fingerprintFilePath))) {
            return reader.readLine().split(" ")[1].trim();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // This new method would retrieve fingerprints from all existing relays
    private List<String> getAllRelayFingerprints() {
        // This path should lead to the base directory where all relay data directories are stored
        String dataDirectoryPath = System.getProperty("user.dir") + File.separator + "torrc" + File.separator + "dataDirectory";
        return getFingerprints(dataDirectoryPath);
    }

    // This new method would update or append the fingerprints to the torrc configuration file
    // This new method would update the MyFamily line with current fingerprints
    private void updateTorrcWithFingerprints(Path torrcFilePath, List<String> currentFingerprints) throws IOException {
        // Read the existing torrc file content
        List<String> fileContent = new ArrayList<>();
        boolean myFamilyUpdated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(torrcFilePath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // If the MyFamily line is encountered, update it with the current fingerprints
                if (line.startsWith("MyFamily")) {
                    if (!currentFingerprints.isEmpty()) {
                        line = "MyFamily " + String.join(", ", currentFingerprints);
                        myFamilyUpdated = true;
                    } else {
                        // If there are no current fingerprints, remove the MyFamily line
                        continue;
                    }
                }
                fileContent.add(line);
            }
        }

        // If MyFamily line was not in the file and we have fingerprints, add it
        if (!myFamilyUpdated && !currentFingerprints.isEmpty()) {
            fileContent.add("MyFamily " + String.join(", ", currentFingerprints));
        }

        // Write the updated content back to the torrc file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(torrcFilePath.toFile()))) {
            for (String line : fileContent) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    @PostMapping("/remove")
    @ResponseBody
    public Map<String, Object> removeRelay(@RequestParam String relayNickname, @RequestParam String relayType) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Build paths for Torrc file and DataDirectory
            Path torrcFilePath = buildTorrcFilePath(relayNickname, relayType);
            String dataDirectoryPath = buildDataDirectoryPath(relayNickname);

            // Delete Torrc file
            Files.deleteIfExists(torrcFilePath);

            // Delete DataDirectory
            FileUtils.deleteDirectory(new File(dataDirectoryPath));

            // Build paths for Onion files in /onion folder and its corresponding file in torrc directory
            Path onionFilePath = Paths.get(System.getProperty("user.dir"), "onion", "hiddenServiceDirs", "onion-service-" + relayNickname);
            Path torrcOnionFilePath = Paths.get(System.getProperty("user.dir"), "torrc", "torrc-" + relayNickname + "_onion");

            // Delete Onion files in /onion folder and its corresponding file in torrc directory
            FileUtils.deleteDirectory(new File(onionFilePath.toString()));
            Files.deleteIfExists(torrcOnionFilePath);

            // Call the shell script to delete Nginx configuration file and symbolic link
            ProcessBuilder processBuilder = new ProcessBuilder("shellScripts/remove_onion_files.sh", relayNickname);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IOException("Failed to delete Nginx configuration file and symbolic link");
            }

            reloadNginx();

            response.put("success", true);
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to remove Torrc file, DataDirectory, Onion files, Nginx configuration file and symbolic link for relayNickname: {}", relayNickname, e);
            response.put("success", false);
        }
        return response;
    }

    private String buildDataDirectoryPath(String relayNickname) {
        return System.getProperty("user.dir") + File.separator + "torrc" + File.separator + "dataDirectory" + File.separator + relayNickname;
    }

    // New method to get the webtunnel link
    private String getWebtunnelLink(String relayNickname) {
        String dataDirectoryPath = buildDataDirectoryPath(relayNickname);
        String fingerprintFilePath = dataDirectoryPath + File.separator + "fingerprint";
        String fingerprint = readFingerprint(fingerprintFilePath);

        // Construct the path to the torrc file
        String torrcFilePath = System.getProperty("user.dir") + File.separator + "torrc" + File.separator + "torrc-" + relayNickname + "_bridge";

        String webtunnelDomainAndPath = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(torrcFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Check if the line starts with "ServerTransportOptions webtunnel url"
                if (line.startsWith("ServerTransportOptions webtunnel url")) {
                    // Extract the webtunnel domain and path from the line
                    webtunnelDomainAndPath = line.split("=")[1].trim();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Replace the "https://yourdomain/path" in the webtunnel link with the extracted webtunnel domain and path
        String webtunnelLink = "webtunnel 10.0.0.2:443 " + fingerprint + " url=" + webtunnelDomainAndPath;

        return webtunnelLink;
    }

    // Method for opening orport of a relay using UPnP
    @PostMapping("/open-orport")
    @ResponseBody
    public Map<String, Object> openOrPort(@RequestParam String relayNickname, @RequestParam String relayType) {
        Map<String, Object> response = new HashMap<>();
        // Build the path to the torrc file
        Path torrcFilePath = buildTorrcFilePath(relayNickname, relayType);

        // Get the orport from the torrc file
        int orPort = getOrPort(torrcFilePath);

        // Open the orport using UPnP
        boolean success = UPnP.openPortTCP(orPort);
        if (success) {
            response.put("success", true);
        } else {
            response.put("success", false);
            response.put("message", "Failed to open ORPort using UPnP");
        }
        return response;
    }

    private int getOrPort(Path torrcFilePath) {
        int orPort = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(torrcFilePath.toFile()))){
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("ORPort")) {
                    orPort = Integer.parseInt(line.split(" ")[1]);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return orPort;
    }

    public void reloadNginx() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "systemctl", "reload", "nginx");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Failed to restart Nginx");
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to restart Nginx", e);
        }
    }

    public void checkAndManageNginxStatus() {
        // Get the list of all webTunnels and Onion services
        List<String> allServices = getAllServices();

        // Iterate over the list and check the status of each service
        for (String service : allServices) {
            String status = getRelayStatus(service, "onion");
            // If at least one service is online, start the Nginx service and return
            if ("online".equals(status)) {
                startNginx();
                return;
            }
        }

        // If no service is online, stop the Nginx service
        stopNginx();
    }

    private List<String> getAllServices() {
        List<String> allServices = new ArrayList<>();
        // Get the list of all onion services
        List<TorConfiguration> onionConfigs = torConfigurationService.readTorConfigurationsFromFolder(torConfigurationService.buildFolderPath(), "onion");
        for (TorConfiguration config : onionConfigs) {
            allServices.add(config.getHiddenServicePort());
        }

        // Get the list of all webTunnels
        List<TorConfiguration> bridgeConfigs = torConfigurationService.readTorConfigurationsFromFolder(torConfigurationService.buildFolderPath(), "bridge");
        for (TorConfiguration config : bridgeConfigs) {
            allServices.add(config.getBridgeRelayConfig().getNickname());
        }
        return allServices;
    }

    public void startNginx() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "systemctl", "start", "nginx");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Failed to start Nginx");
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to start Nginx", e);
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
            logger.error("Failed to stop Nginx", e);
        }
    }
}
