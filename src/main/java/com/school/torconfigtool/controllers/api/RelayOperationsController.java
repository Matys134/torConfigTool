package com.school.torconfigtool.controllers.api;

import com.school.torconfigtool.models.TorConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/relay-operations")
public class RelayOperationsController {

    private static final Logger logger = LoggerFactory.getLogger(RelayOperationsController.class);

    private Map<String, Integer> relayPids = new HashMap<>();

    private List<TorConfiguration> guardConfigs = new ArrayList<>();
    private List<TorConfiguration> bridgeConfigs = new ArrayList<>();

    @GetMapping
    public String relayOperations(Model model) {
        guardConfigs = readTorConfigurations("torrc/guard", "guard");
        bridgeConfigs = readTorConfigurations("torrc/bridge", "bridge");

        model.addAttribute("guardConfigs", guardConfigs);
        model.addAttribute("bridgeConfigs", bridgeConfigs);

        return "relay-operations"; // Thymeleaf template name
    }

    private List<TorConfiguration> readTorConfigurations(String folder, String relayType) {
        List<TorConfiguration> configs = new ArrayList<>();
        File[] files = new File(folder).listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        TorConfiguration config = parseTorConfiguration(file);
                        config.setRelayType(relayType); // Set the relay type
                        configs.add(config);
                    } catch (IOException e) {
                        // Handle or log the exception
                        logger.error("Error reading Tor configuration file", e);
                    }
                }
            }
        }
        return configs;
    }

    private TorConfiguration parseTorConfiguration(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            TorConfiguration config = new TorConfiguration();
            String line;

            while ((line = reader.readLine()) != null) {
                parseTorConfigLine(line, config);
            }

            return config;
        }
    }

    private void parseTorConfigLine(String line, TorConfiguration config) {
        if (line.startsWith("Nickname")) {
            config.setNickname(line.split("Nickname")[1].trim());
        } else if (line.startsWith("ORPort")) {
            config.setOrPort(line.split("ORPort")[1].trim());
        } else if (line.startsWith("Contact")) {
            config.setContact(line.split("Contact")[1].trim());
        } else if (line.startsWith("HiddenServiceDir")) {
            // Onion service specific
            config.setHiddenServiceDir(line.split("HiddenServiceDir")[1].trim());
        } else if (line.startsWith("HiddenServicePort")) {
            // Onion service specific
            config.setHiddenServicePort(line.split("HiddenServicePort")[1].trim());
        } else if (line.startsWith("ControlPort")) {
            config.setControlPort(line.split("ControlPort")[1].trim());
        } else if (line.startsWith("SocksPort")) {
            config.setSocksPort(line.split("SocksPort")[1].trim());
        } else if (line.startsWith("BandwidthRate")) {
            config.setBandwidthRate(line.split("BandwidthRate")[1].trim());
        } else if (line.startsWith("ServerTransportListenAddr obfs4 0.0.0.0:")) {
            // Bridge specific
            config.setBridgeTransportListenAddr(line.split("ServerTransportListenAddr obfs4")[1].trim());
        }
    }

    @PostMapping("/stop")
    public String stopRelay(@RequestParam String relayNickname, @RequestParam String relayType, Model model) {
        try {
            String torrcFilePath = buildTorrcFilePath(relayNickname, relayType);
            File torrcFile = new File(torrcFilePath);

            if (torrcFile.exists()) {
                int pid = getTorRelayPID(torrcFilePath);
                String stopCommand = "kill -SIGINT " + pid;
                int exitCode = executeCommand(stopCommand);

                if (exitCode == 0) {
                    model.addAttribute("successMessage", "Tor Relay stopped successfully!");
                } else {
                    model.addAttribute("errorMessage", "Failed to stop Tor Relay service.");
                    logger.error("Failed to stop Tor Relay for relayNickname: {}", relayNickname);
                }
            } else {
                model.addAttribute("errorMessage", "Torrc file does not exist for relay: " + relayNickname);
            }
        } catch (Exception e) {
            logger.error("Failed to stop Tor Relay for relayNickname: {}", relayNickname, e);
            handleException(model, "Failed to stop Tor Relay.", e);
        }
        return "relay-operations";
    }


    @PostMapping("/start")
    public String startRelay(@RequestParam String relayNickname, @RequestParam String relayType, Model model) {
        try {
            String torrcFilePath = buildTorrcFilePath(relayNickname, relayType);
            File torrcFile = new File(torrcFilePath);
            System.out.println(torrcFilePath);

            if (torrcFile.exists()) {
                String command = "tor -f " + torrcFile.getAbsolutePath();
                int pid = executeCommand(command);
                relayPids.put(relayNickname, pid);
            } else {
                model.addAttribute("errorMessage", "Torrc file does not exist for relay: " + relayNickname);
            }
        } catch (Exception e) {
            logger.error("Failed to start Tor Relay for relayNickname: {}", relayNickname, e);
            handleException(model, "Failed to start Tor Relay.", e);
        }
        return "relay-operations";
    }

    // Update the buildTorrcFilePath method to consider the relay type
    private String buildTorrcFilePath(String relayNickname, String relayType) {
        String currentDirectory = System.getProperty("user.dir");
        String torrcFileName = "local-torrc-" + relayNickname;
        String folder = relayType.equals("guard") ? "guard" : "bridge";
        return currentDirectory + File.separator + "torrc" + File.separator + folder + File.separator + torrcFileName;
    }

    private void handleException(Model model, String errorMessage, Exception e) {
        e.printStackTrace();
        model.addAttribute("errorMessage", errorMessage);
    }

    @GetMapping("/status")
    @ResponseBody
    public String getRelayStatus(@RequestParam String relayNickname, @RequestParam String relayType) {
        String torrcFilePath = buildTorrcFilePath(relayNickname, relayType);
        int pid = getTorRelayPID(torrcFilePath);
        if (pid > 0) {
            return "online";
        } else if (pid == -1) {
            return "offline";
        } else {
            return "error"; // Handle other cases as needed
        }
    }

    private int getTorRelayPID(String torrcFileName) {
        String relayNickname = new File(torrcFileName).getName(); // Extract the file name (relay nickname)
        try {
            String command = "ps aux | grep " + relayNickname + " | grep -v grep | awk '{print $2}'";
            System.out.println(command);
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String pidString = reader.readLine();

            if (pidString != null && !pidString.isEmpty()) {
                int pid = Integer.parseInt(pidString);
                process.waitFor();
                return pid;
            } else {
                return -1; // Return -1 to indicate that no PID was found
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }


    public class RelayOperationException extends RuntimeException {
        public RelayOperationException(String message) {
            super(message);
        }
    }

    private int executeCommand(String command) throws IOException, InterruptedException {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            return process.waitFor();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }
}
