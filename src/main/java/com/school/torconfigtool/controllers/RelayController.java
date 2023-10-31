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

            if (!new File(torrcFilePath).exists()) {
                createTorrcFile(torrcFilePath, relayNickname, relayBandwidth, relayPort, relayContact, controlPort, socksPort);
            }

            model.addAttribute("successMessage", "Tor Relay configured successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to configure Tor Relay.");
        }

        return "relay-config";
    }

    private void createTorrcFile(String filePath, String relayNickname, Integer relayBandwidth, int relayPort, String relayContact, int controlPort, int socksPort) {
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

            String currentDirectory = System.getProperty("user.dir");
            String dataDirectoryPath = currentDirectory + File.separator + "torrc" + File.separator + "dataDirectory" + File.separator + relayNickname;
            writer.write("DataDirectory " + dataDirectoryPath);

            List<String> fingerprints = getFingerprints(dataDirectoryPath);
            if (!fingerprints.isEmpty()) {
                writer.newLine();
                writer.write("MyFamily " + String.join(", ", fingerprints));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getFingerprints(String dataDirectoryPath) {
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

    private void checkRunningRelays() {
        try {
            Process process = Runtime.getRuntime().exec("ps aux");
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            List<Integer> runningRelayPIDs = reader.lines()
                    .filter(line -> line.contains("tor -f local-torrc-"))
                    .map(line -> line.split("\\s+"))
                    .filter(parts -> parts.length >= 2)
                    .map(parts -> Integer.parseInt(parts[1]))
                    .collect(Collectors.toList());

            runningRelayPIDs.forEach(pid -> System.out.println("PID: " + pid));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
