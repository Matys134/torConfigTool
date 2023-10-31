package com.school.torconfigtool.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/relay")
public class RelayController {

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
                                 @RequestParam int controlPort,
                                 @RequestParam int socksPort,
                                 Model model) {
        try {
            // Define the path to the torrc file based on the relay nickname
            String torrcFileName = "local-torrc-" + relayNickname;
            String torrcFilePath = "torrc/guard/" + torrcFileName;

            // Check if the torrc file exists, create it if not
            if (!new File(torrcFilePath).exists()) {
                createTorrcFile(torrcFilePath, relayNickname, relayBandwidth, relayPort, relayContact, controlPort, socksPort);
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


    public void createTorrcFile(String filePath, String relayNickname, Integer relayBandwidth, int relayPort, String relayContact, int controlPort, int socksPort) throws IOException {
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
            writer.write("ControlPort " + controlPort);
            writer.newLine();
            writer.write("SocksPort " + socksPort);
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
                if (line.contains("tor -f local-torrc-")) {
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
