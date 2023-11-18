package com.school.torconfigtool.controllers;

import com.school.torconfigtool.models.BaseRelayConfig;
import com.school.torconfigtool.models.GuardRelayConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/relay")
public class RelayController {

    @GetMapping
    public String relayConfigurationForm(Model model) {
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

            // Check if the ports are available
            if (!portsAreAvailable(relayPort, controlPort, socksPort)) {
                model.addAttribute("errorMessage", "One or more ports are already in use.");
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

            // Use the new method to write specific configurations
            config.writeSpecificConfig(writer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void checkRunningRelays() {
        try {
            Process process = Runtime.getRuntime().exec("ps aux");
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            List<Integer> runningRelayPIDs = reader.lines().filter(line -> line.contains("tor -f local-torrc-")).map(line -> line.split("\\s+")).filter(parts -> parts.length >= 2).map(parts -> Integer.parseInt(parts[1])).toList();

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

    // Check if the ports are available by checking torrc files and running processes
    private boolean portsAreAvailable(int relayPort, int controlPort, int socksPort) {
        String currentDirectory = System.getProperty("user.dir");
        String torrcDirectory = currentDirectory + File.separator + "torrc" + File.separator + "guard";

        File[] torrcFiles = new File(torrcDirectory).listFiles();
        if (torrcFiles != null) {
            for (File file : torrcFiles) {
                if (file.isFile() && file.getName().startsWith("local-torrc-")) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("ORPort") && line.contains(String.valueOf(relayPort))) {
                                return false;
                            } else if (line.startsWith("ControlPort") && line.contains(String.valueOf(controlPort))) {
                                return false;
                            } else if (line.startsWith("SocksPort") && line.contains(String.valueOf(socksPort))) {
                                return false;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        try {
            Process process = Runtime.getRuntime().exec("ps aux");
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            List<Integer> runningRelayPIDs = reader.lines().filter(line -> line.contains("tor -f local-torrc-")).map(line -> line.split("\\s+")).filter(parts -> parts.length >= 2).map(parts -> Integer.parseInt(parts[1])).toList();

            for (Integer pid : runningRelayPIDs) {
                String command = String.format("netstat -tulpn | grep %d", pid);
                Process netstatProcess = Runtime.getRuntime().exec(command);
                InputStream netstatInputStream = netstatProcess.getInputStream();
                BufferedReader netstatReader = new BufferedReader(new InputStreamReader(netstatInputStream));

                List<String> netstatOutput = netstatReader.lines().toList();
                for (String netstatLine : netstatOutput) {
                    if (netstatLine.contains(String.valueOf(relayPort)) || netstatLine.contains(String.valueOf(controlPort)) || netstatLine.contains(String.valueOf(socksPort))) {
                        return false;
                    }
                }
            }
    } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }
}

