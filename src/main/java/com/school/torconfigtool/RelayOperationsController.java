package com.school.torconfigtool;

import com.school.torconfigtool.exception.RelayOperationException;
import com.school.torconfigtool.util.ProcessManagementService;
import com.simtechdata.waifupnp.UPnP;
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

@Controller
@RequestMapping("/relay-operations")
public class RelayOperationsController {

    private static final Logger logger = LoggerFactory.getLogger(RelayOperationsController.class);
    private final TorConfigurationService torConfigurationService;
    private final ProcessManagementService processManagementService;
    private final FileManager fileManager = new FileManager();

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
        model.addAttribute("guardConfigs", getConfigs("guard"));
        model.addAttribute("bridgeConfigs", getConfigs("bridge"));
        model.addAttribute("onionConfigs", getConfigs("onion"));
        model.addAttribute("hostnames", getHostnames());
        model.addAttribute("webtunnelLinks", getWebtunnelLinks());
        model.addAttribute("upnpPorts", getUPnPPorts());

        return "relay-operations";
    }

    private List<TorConfiguration> getOnionConfigs() {
        return torConfigurationService.readTorConfigurationsFromFolder(torConfigurationService.buildFolderPath(), "onion");
    }

    private Map<String, String> getHostnames() {
        List<TorConfiguration> onionConfigs = getOnionConfigs();
        return createHostnamesMap(onionConfigs);
    }

    private Map<String, String> createHostnamesMap(List<TorConfiguration> onionConfigs) {
        Map<String, String> hostnames = new HashMap<>();
        for (TorConfiguration config : onionConfigs) {
            String hostname = fileManager.readHostnameFile(config.getHiddenServicePort());
            hostnames.put(config.getHiddenServicePort(), hostname);
        }
        return hostnames;
    }

    private Map<String, String> getWebtunnelLinks() {
        Map<String, String> webtunnelLinks = new HashMap<>();
        List<TorConfiguration> bridgeConfigs = torConfigurationService.readTorConfigurationsFromFolder(torConfigurationService.buildFolderPath(), "bridge");
        for (TorConfiguration config : bridgeConfigs) {
            String webtunnelLink = getWebtunnelLink(config.getBridgeRelayConfig().getNickname());
            webtunnelLinks.put(config.getBridgeRelayConfig().getNickname(), webtunnelLink);
        }
        return webtunnelLinks;
    }

    private List<Integer> getUPnPPorts() {
        List<Integer> upnpPorts = new ArrayList<>();
        List<TorConfiguration> guardConfigs = torConfigurationService.readTorConfigurationsFromFolder(torConfigurationService.buildFolderPath(), "guard");
        for (TorConfiguration config : guardConfigs) {
            Path torrcFilePath = buildTorrcFilePath(config.getGuardRelayConfig().getNickname(), "guard");
            int orPort = fileManager.getOrPort(torrcFilePath);
            upnpPorts.add(orPort);
        }
        return upnpPorts;
    }


    @PostMapping("/stop")
    public String stopRelay(@RequestParam String relayNickname, @RequestParam String relayType, Model model) {
        return changeRelayStateAndManagePort(relayNickname, relayType, model, false);
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
        return changeRelayStateAndManagePort(relayNickname, relayType, model, true);
    }

    private String changeRelayStateWithoutFingerprint(String relayNickname, String relayType, Model model) {
        Path torrcFilePath = buildTorrcFilePath(relayNickname, relayType);
        String operation = "start";
        try {
            processRelayOperationWithoutFingerprint(torrcFilePath, relayNickname);
            model.addAttribute("successMessage", "Tor Relay " + operation + "ed successfully!");
        } catch (RelayOperationException | IOException | InterruptedException e) {
            logger.error("Failed to {} Tor Relay for relayNickname: {}", operation, relayNickname, e);
            model.addAttribute("errorMessage", "Failed to " + operation + " Tor Relay.");
        }
        return relayOperations(model);
    }

    private void processRelayOperationWithoutFingerprint(Path torrcFilePath, String relayNickname) throws IOException, InterruptedException {
        if (!torrcFilePath.toFile().exists()) {
            throw new RelayOperationException("Torrc file does not exist for relay: " + relayNickname);
        }
        {
            // Step 3: Start the Relay
            String command = "tor -f " + torrcFilePath.toAbsolutePath();
            System.out.println("Executing command: " + command);
            int exitCode = processManagementService.executeCommand(command);
            if (exitCode != 0) {
                throw new RelayOperationException("Failed to start Tor Relay service.");
            }
        }
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
            List<String> allFingerprints = fileManager.getAllRelayFingerprints();

            // Step 2: Update the torrc File with fingerprints
            fileManager.updateTorrcWithFingerprints(torrcFilePath, allFingerprints);

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


    @PostMapping("/remove")
    @ResponseBody
    public Map<String, Object> removeRelay(@RequestParam String relayNickname, @RequestParam String relayType) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Build paths for Torrc file and DataDirectory
            Path torrcFilePath = buildTorrcFilePath(relayNickname, relayType);
            String dataDirectoryPath = fileManager.buildDataDirectoryPath(relayNickname);

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

    // New method to get the webtunnel link
    private String getWebtunnelLink(String relayNickname) {
        String dataDirectoryPath = fileManager.buildDataDirectoryPath(relayNickname);
        String fingerprintFilePath = dataDirectoryPath + File.separator + "fingerprint";
        String fingerprint = fileManager.readFingerprint(fingerprintFilePath);

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

        return "webtunnel 10.0.0.2:443 " + fingerprint + " url=" + webtunnelDomainAndPath;
    }

    // Method for opening orport of a relay using UPnP
    @PostMapping("/open-orport")
    @ResponseBody
    public Map<String, Object> openOrPort(@RequestParam String relayNickname, @RequestParam String relayType) {
        Map<String, Object> response = new HashMap<>();
        // Build the path to the torrc file
        Path torrcFilePath = buildTorrcFilePath(relayNickname, relayType);

        // Get the orport from the torrc file
        int orPort = fileManager.getOrPort(torrcFilePath);

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

    @PostMapping("/toggle-upnp")
    @ResponseBody
    public Map<String, Object> toggleUPnP(@RequestParam boolean enable) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get the list of all guard relays
            List<TorConfiguration> guardConfigs = torConfigurationService.readTorConfigurationsFromFolder(torConfigurationService.buildFolderPath(), "guard");
            for (TorConfiguration config : guardConfigs) {
                if (enable) {
                    String status = getRelayStatus(config.getGuardRelayConfig().getNickname(), "guard");
                    if ("online".equals(status)) {
                        // Open the ORPort
                        openOrPort(config.getGuardRelayConfig().getNickname(), "guard");
                    }
                } else {
                    // Close the ORPort
                    closeOrPort(config.getGuardRelayConfig().getNickname(), "guard");
                }
            }
            response.put("success", true);
            response.put("message", "UPnP for Guard Relays " + (enable ? "enabled" : "disabled") + " successfully!");
        } catch (Exception e) {
            logger.error("Failed to " + (enable ? "enable" : "disable") + " UPnP for Guard Relays", e);
            response.put("success", false);
            response.put("message", "Failed to " + (enable ? "enable" : "disable") + " UPnP for Guard Relays.");
        }
        return response;
    }

    private void closeOrPort(String relayNickname, String relayType) {
        Path torrcFilePath = buildTorrcFilePath(relayNickname, relayType);
        int orPort = fileManager.getOrPort(torrcFilePath);
        UPnP.closePortTCP(orPort);
    }

    private List<TorConfiguration> getConfigs(String configType) {
        return torConfigurationService.readTorConfigurationsFromFolder(torConfigurationService.buildFolderPath(), configType);
    }

    private String changeRelayStateAndManagePort(String relayNickname, String relayType, Model model, boolean start) {
        String view;
        if ("guard".equals(relayType)) {
            view = changeRelayState(relayNickname, relayType, model, start);
        } else {
            view = changeRelayStateWithoutFingerprint(relayNickname, relayType, model);
        }

        new Thread(() -> {
            try {
                String expectedStatus = start ? "online" : "offline";
                waitForStatusChange(relayNickname, relayType, expectedStatus);
                if (start) {
                    openOrPort(relayNickname, relayType);
                } else {
                    closeOrPort(relayNickname, relayType);
                }
            } catch (InterruptedException e) {
                logger.error("Error while waiting for relay to " + (start ? "start" : "stop"), e);
            }
        }).start();

        return view;
    }
}
