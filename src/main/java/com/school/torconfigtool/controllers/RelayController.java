package com.school.torconfigtool.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/relay")
public class RelayController {

    private Map<String, Integer> relayPids = new HashMap<>();

    @GetMapping
    public String relayConfigurationForm() {
        System.out.println("Relay configuration form requested");
        checkRunningRelays();
        return "relay-config"; // Thymeleaf template name (relay-config.html)
    }

    @PostMapping("/configure")
    public String configureRelay(@RequestParam String relayNickname,
                                 @RequestParam(required = false) Integer relayBandwidth,
                                 @RequestParam int relayPort,
                                 @RequestParam String relayContact,
                                 Model model) {
        try {
            // Define the path to the torrc file based on the relay nickname
            String torrcFileName = "local-torrc-" + relayNickname;
            String torrcFilePath = "torrc/guard/" + torrcFileName;

            // Check if the torrc file exists, create it if not
            if (!new File(torrcFilePath).exists()) {
                createTorrcFile(torrcFilePath, relayNickname, relayBandwidth, relayPort, relayContact);
            }

            // Restart the Tor service with the new configuration if necessary

            // Provide a success message
            model.addAttribute("successMessage", "Tor Relay configured successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to configure Tor Relay.");
        }

        return "relay-config"; // Thymeleaf template name (relay-config.html)
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
                Process process = Runtime.getRuntime().exec(command);

                // Store the process ID in the relayPids map
                int pid = getTorRelayPID("local-torrc-" + relayNickname);
                relayPids.put(relayNickname, pid);

                // Wait for the process to complete
                /*int exitCode = process.waitFor();

                if (exitCode == 0) {
                    model.addAttribute("successMessage", "Tor Relay started successfully!");
                } else {
                    model.addAttribute("errorMessage", "Failed to start Tor Relay service.");
                }*/
            } else {
                model.addAttribute("errorMessage", "Torrc file does not exist for relay: " + relayNickname);
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to start Tor Relay.");
        }

        return "relay-config"; // Redirect to the configuration page
    }

    private int getTorRelayPID(String torrcFileName) throws IOException, InterruptedException {
        String command = "ps aux | grep " + torrcFileName + " | grep -v grep | awk '{print $2}'";
        Process process = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", command});
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        System.out.println("PID command: " + command);

        String pidString = reader.readLine();
        int pid = Integer.parseInt(pidString);
        process.waitFor();
        System.out.println("PID: " + pid);
        return pid;
    }

    @PostMapping("/stop")
    public String stopRelay(@RequestParam String relayNickname, @RequestParam int pid, Model model) {
        try {
            // Execute a command to stop the Tor service using the provided PID
            String stopCommand = "sudo systemctl stop " + pid;
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
        return "relay-config"; // Redirect to the configuration page
    }

    private boolean stopTorRelayService() {
        try {
            // Execute a command to stop the Tor service
            Process process = Runtime.getRuntime().exec("sudo systemctl stop tor");

            // Wait for the process to complete
            int exitCode = process.waitFor();

            // Check the exit code to determine if the stop was successful
            return exitCode == 0;
        } catch (Exception e) {
            e.printStackTrace();
            // Log and handle any exceptions that occur during the stop
            return false;
        }
    }

    public void createTorrcFile(String filePath, String relayNickname, Integer relayBandwidth, int relayPort, String relayContact) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Nickname " + relayNickname);
            writer.newLine();
            if (relayBandwidth != null) {
                writer.write("BandwidthRate " + relayBandwidth + " KBytes");
                writer.newLine();
            }
            writer.write("ORPort " + relayPort);
            writer.newLine();
            writer.write("ContactInfo " + relayContact);
            writer.newLine();

            // Get the program's current working directory
            String currentDirectory = System.getProperty("user.dir");

            // Define the DataDirectory path based on the current working directory
            String dataDirectoryPath = currentDirectory + File.separator + "torrc" + File.separator + "dataDirectory" + File.separator + relayNickname;

            // Write the DataDirectory configuration line
            writer.write("DataDirectory " + dataDirectoryPath);

            // Get the list of files in the "torrc/guard" directory
            File guardDirectory = new File(currentDirectory + File.separator + "torrc" + File.separator + "guard");
            File[] guardFiles = guardDirectory.listFiles();

            if (guardFiles != null && guardFiles.length > 1) {
                // If there are more than one relay in the "torrc/guard" directory, add MyFamily line
                writer.newLine();
                writer.write("MyFamily ");

                // Iterate through the "dataDirectory" folders
                File dataDirectory = new File(dataDirectoryPath);
                File[] dataDirectoryFiles = dataDirectory.listFiles(File::isDirectory);

                if (dataDirectoryFiles != null) {
                    for (File dataDir : dataDirectoryFiles) {
                        String fingerprintFilePath = dataDir.getAbsolutePath() + File.separator + "fingerprint";
                        String fingerprint = readFingerprint(fingerprintFilePath);
                        if (fingerprint != null) {
                            writer.write(fingerprint);
                            writer.write(", ");
                        }
                    }
                }
            }
        }
    }

    private String readFingerprint(String fingerprintFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fingerprintFilePath))) {
            return reader.readLine().split(" ")[1].trim();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void checkRunningRelays() {
        try {
            // Execute the 'ps' command to list running processes
            Process process = Runtime.getRuntime().exec("ps aux");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            List<String> runningRelayLines = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                if (line.contains("tor")) {
                    runningRelayLines.add(line);
                }
            }

            // Parse the runningRelayLines to extract PIDs and other relevant information
            List<Integer> runningRelayPIDs = new ArrayList<>();
            for (String relayLine : runningRelayLines) {
                // Extract PID and other information from the relayLine
                String[] parts = relayLine.split("\\s+"); // Split by whitespace
                if (parts.length >= 2) {
                    int pid = Integer.parseInt(parts[1]);
                    runningRelayPIDs.add(pid);
                }
                // Add more parsing logic if needed
            }

            // Store the PIDs or perform other actions as needed
            for (Integer pid : runningRelayPIDs) {
                System.out.println("PID: " + pid);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
