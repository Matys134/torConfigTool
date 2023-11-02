package com.school.torconfigtool.controllers.api;

import com.school.torconfigtool.models.TorConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/relay-operations")
public class RelayOperationsController {

    private Map<String, Integer> relayPids = new HashMap<>();

    @GetMapping
    public String relayOperations(Model model) {
        List<TorConfiguration> guardConfigs = readTorConfigurations("torrc/guard");
        List<TorConfiguration> onionConfigs = readTorConfigurations("torrc/onion");

        model.addAttribute("guardConfigs", guardConfigs);
        model.addAttribute("onionConfigs", onionConfigs);

        return "relay-operations"; // Thymeleaf template name
    }

    private List<TorConfiguration> readTorConfigurations(String folder) {
        List<TorConfiguration> configs = new ArrayList<>();
        File[] files = new File(folder).listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    try {
                        TorConfiguration config = parseTorConfiguration(file);
                        configs.add(config);
                    } catch (IOException e) {
                        e.printStackTrace();
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
        }
    }

    @PostMapping("/stop")
    public String stopRelay(@RequestParam String relayNickname, Model model) {
        try {
            int pid = getTorRelayPID("local-torrc-" + relayNickname);
            String stopCommand = "kill -SIGINT " + pid;
            Process stopProcess = Runtime.getRuntime().exec(stopCommand);

            int exitCode = stopProcess.waitFor();

            if (exitCode == 0) {
                model.addAttribute("successMessage", "Tor Relay stopped successfully!");
            } else {
                model.addAttribute("errorMessage", "Failed to stop Tor Relay service.");
            }
        } catch (Exception e) {
            handleException(model, "Failed to stop Tor Relay.", e);
        }
        return "relay-operations";
    }

    @PostMapping("/start")
    public String startRelay(@RequestParam String relayNickname, Model model) {
        try {
            String torrcFilePath = buildTorrcFilePath(relayNickname);
            File torrcFile = new File(torrcFilePath);

            if (torrcFile.exists()) {
                String command = "tor -f " + torrcFile.getAbsolutePath();
                Process process = Runtime.getRuntime().exec(command);
                int pid = getTorRelayPID("local-torrc-" + relayNickname);
                relayPids.put(relayNickname, pid);
            } else {
                model.addAttribute("errorMessage", "Torrc file does not exist for relay: " + relayNickname);
            }
        } catch (Exception e) {
            handleException(model, "Failed to start Tor Relay.", e);
        }
        return "relay-operations";
    }

    private String buildTorrcFilePath(String relayNickname) {
        String currentDirectory = System.getProperty("user.dir");
        String torrcFileName = "local-torrc-" + relayNickname;
        return currentDirectory + File.separator + "torrc" + File.separator + "guard" + File.separator + torrcFileName;
    }

    private void handleException(Model model, String errorMessage, Exception e) {
        e.printStackTrace();
        model.addAttribute("errorMessage", errorMessage);
    }

    @GetMapping("/status")
    @ResponseBody
    public String getRelayStatus(@RequestParam String relayNickname) {
        int pid = getTorRelayPID("local-torrc-" + relayNickname);
        if (pid > 0) {
            return "online";
        } else if (pid == -1) {
            return "offline";
        } else {
            return "error"; // Handle other cases as needed
        }
    }

    private int getTorRelayPID(String torrcFileName) {
        try {
            String command = "ps aux | grep " + torrcFileName + " | grep -v grep | awk '{print $2}'";
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
}
