package com.school.torconfigtool.controllers;

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
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        TorConfiguration config = new TorConfiguration();

                        String line;
                        while ((line = reader.readLine()) != null) {
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
                            }
                        }

                        configs.add(config);
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return configs;
    }

    @PostMapping("/stop")
    public String stopRelay(@RequestParam String relayNickname, Model model) {
        try {

            int pid = getTorRelayPID("local-torrc-" + relayNickname);
            // Execute a command to stop the Tor service using the provided PID
            String stopCommand = "kill -SIGINT " + pid;
            Process stopProcess = Runtime.getRuntime().exec(stopCommand);

            System.out.println("Stop command: " + stopCommand);

            int exitCode = stopProcess.waitFor();

            if (exitCode == 0) {
                // Relay stopped successfully
                model.addAttribute("successMessage", "Tor Relay stopped successfully!");
            } else {
                model.addAttribute("errorMessage", "Failed to stop Tor Relay service.");
            }
        } catch (Exception e) {
            // Error handling
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to stop Tor Relay.");
        }

        // Return the template or a redirect as needed
        return "relay-operations"; // Redirect to the configuration page
    }

    @PostMapping("/start")
    public String startRelay(@RequestParam String relayNickname, Model model) {
        try {
            // Determine the program's current working directory
            String currentDirectory = System.getProperty("user.dir");

            // Define the path to the torrc file based on the relay nickname
            String torrcFileName = "local-torrc-" + relayNickname;
            String torrcFilePath = currentDirectory + File.separator + "torrc" + File.separator + "guard" + File.separator + torrcFileName;


            // Check if the torrc file exists
            File torrcFile = new File(torrcFilePath);

            if (torrcFile.exists()) {
                // Build the command to start the Tor service with the custom torrc file
                String command = "tor -f " + torrcFile.getAbsolutePath();

                // Execute the command
                Runtime.getRuntime().exec(command);

                // Store the process ID in the relayPids map
                int pid = getTorRelayPID("local-torrc-" + relayNickname);
                relayPids.put(relayNickname, pid);

                // Wait for the process to complete
            } else {
                model.addAttribute("errorMessage", "Torrc file does not exist for relay: " + relayNickname);
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to start Tor Relay.");
        }

        return "relay-operations"; // Redirect to the configuration page
    }

    private int getTorRelayPID(String torrcFileName) throws IOException, InterruptedException {
        String command = "ps aux | grep " + torrcFileName + " | grep -v grep | awk '{print $2}'";
        Process process = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", command});
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        System.out.println("PID command: " + command);

        String pidString = reader.readLine();

        if (pidString != null && !pidString.isEmpty()) {
            int pid = Integer.parseInt(pidString);
            process.waitFor();
            System.out.println("PID: " + pid);
            return pid;
        } else {
            System.out.println("No PID found for " + torrcFileName);
            return -1; // Return -1 to indicate that no PID was found
        }
    }


    @GetMapping("/status")
    @ResponseBody
    public String getRelayStatus(@RequestParam String relayNickname) throws IOException, InterruptedException {
        int pid = getTorRelayPID("local-torrc-" + relayNickname);
        if (pid > 0) {
            return "online";
        } else if (pid == -1) {
            return "offline";
        } else {
            return "error"; // Handle other cases as needed
        }
    }
}
