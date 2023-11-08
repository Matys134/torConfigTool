package com.school.torconfigtool.controllers;

import com.school.torconfigtool.models.BaseRelayConfig;
import com.school.torconfigtool.models.BridgeRelayConfig;
import com.school.torconfigtool.models.GuardRelayConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/relay")
public class RelayController {

    @GetMapping
    public String relayConfigurationForm() {
        System.out.println("Relay configuration form requested");
        checkRunningRelays();
        return "relay-config";
    }

    @PostMapping("/configure")
    public String configureRelay(@RequestParam String relayNickname,
                                 @RequestParam(required = false) Integer relayBandwidth,
                                 @RequestParam int relayPort,
                                 @RequestParam String relayContact,
                                 @RequestParam int controlPort,
                                 @RequestParam int socksPort,
                                 Model model) {
        try {
            String torrcFileName = "local-torrc-" + relayNickname;
            String torrcFilePath = "torrc/guard/" + torrcFileName;

            // Check if a relay with the same nickname already exists
            if (relayExists(relayNickname)) {
                model.addAttribute("errorMessage", "A relay with the same nickname already exists.");
                return "relay-config";
            }

            GuardRelayConfig config = new GuardRelayConfig(); // Or BridgeRelayConfig depending on the context
            config.setNickname(relayNickname);
            config.setOrPort(String.valueOf(relayPort)); // Assuming the orPort is a String. Convert as necessary
            config.setContact(relayContact);
            config.setControlPort(String.valueOf(controlPort));
            config.setSocksPort(String.valueOf(socksPort));
            // Set other properties as necessary, for example, bandwidth rate

            if (!new File(torrcFilePath).exists()) {
                createTorrcFile(torrcFilePath, config); // Call the refactored method
            }

            model.addAttribute("successMessage", "Tor Relay configured successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to configure Tor Relay.");
        }

        return "relay-config";
    }

    public void createTorrcFile(String filePath, BaseRelayConfig config) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Nickname " + config.getNickname());
            writer.newLine();
            writer.write("ORPort " + config.getOrPort());
            writer.newLine();
            writer.write("ContactInfo " + config.getContact());
            writer.newLine();
            writer.write("ControlPort " + config.getControlPort());
            writer.newLine();
            writer.write("SocksPort " + config.getSocksPort());
            writer.newLine();

            // Add any other common configurations from BaseRelayConfig

            String currentDirectory = System.getProperty("user.dir");
            String dataDirectoryPath = currentDirectory + File.separator + "torrc" + File.separator + "dataDirectory" + File.separator + config.getNickname();
            writer.write("DataDirectory " + dataDirectoryPath);

            // If the relay config has specific fields (like Guard or Bridge), handle them here
            if (config instanceof GuardRelayConfig) {
                // Handle Guard-specific config
                GuardRelayConfig guardConfig = (GuardRelayConfig) config;
                // Write guard-specific configurations to the file
            } else if (config instanceof BridgeRelayConfig) {
                // Handle Bridge-specific config
                BridgeRelayConfig bridgeConfig = (BridgeRelayConfig) config;
                // Write bridge-specific configurations to the file
                writer.write("ServerTransportListenAddr obfs4 " + bridgeConfig.getBridgeTransportListenAddr());
                writer.newLine();
                // Include other bridge-specific settings
            }

            // Add any other specific configurations for different types of relays
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkRunningRelays() {
        try {
            Process process = Runtime.getRuntime().exec("ps aux");
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            List<Integer> runningRelayPIDs = reader.lines().filter(line -> line.contains("tor -f local-torrc-")).map(line -> line.split("\\s+")).filter(parts -> parts.length >= 2).map(parts -> Integer.parseInt(parts[1])).collect(Collectors.toList());

            runningRelayPIDs.forEach(pid -> System.out.println("PID: " + pid));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean relayExists(String relayNickname) {
        String currentDirectory = System.getProperty("user.dir");
        String torrcDirectory = currentDirectory + File.separator + "torrc" + File.separator + "guard";

        File[] torrcFiles = new File(torrcDirectory).listFiles();
        if (torrcFiles != null) {
            for (File file : torrcFiles) {
                if (file.isFile() && file.getName().startsWith("local-torrc-")) {
                    String existingNickname = file.getName().substring("local-torrc-".length());
                    if (existingNickname.equals(relayNickname)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
