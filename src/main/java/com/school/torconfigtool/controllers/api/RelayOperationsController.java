package com.school.torconfigtool.controllers.api;

import com.school.torconfigtool.RelayOperationException;
import com.school.torconfigtool.models.TorConfiguration;
import com.school.torconfigtool.service.ProcessManagementService;
import com.school.torconfigtool.service.TorConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
    }

    @GetMapping
    public String relayOperations(Model model) {
        model.addAttribute("guardConfigs", torConfigurationService.readTorConfigurations("guard"));
        model.addAttribute("bridgeConfigs", torConfigurationService.readTorConfigurations("bridge"));
        model.addAttribute("onionConfigs", torConfigurationService.readTorConfigurations("onion"));
        return "relay-operations";
    }

    @PostMapping("/stop")
    public String stopRelay(@RequestParam String relayNickname, @RequestParam String relayType, Model model) {
        return changeRelayState(relayNickname, relayType, model, false);
    }

    @PostMapping("/start")
    public String startRelay(@RequestParam String relayNickname, @RequestParam String relayType, Model model) {
        return changeRelayState(relayNickname, relayType, model, true);
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
        String folder = relayType.equals("guard") ? "guard" : "bridge";
        return Paths.get(System.getProperty("user.dir"), "torrc", folder, "local-torrc-" + relayNickname);
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
    private void updateTorrcWithFingerprints(Path torrcFilePath, List<String> fingerprints) throws IOException {
        // Read the existing torrc file content
        List<String> fileContent = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(torrcFilePath.toFile()))) {
            String line;
            boolean myFamilyExists = false;
            while ((line = reader.readLine()) != null) {
                // Check if MyFamily line exists
                if (line.startsWith("MyFamily")) {
                    myFamilyExists = true;
                    // Update MyFamily line with new fingerprints, avoiding duplicates
                    String existingFingerprints = line.substring("MyFamily".length()).trim();
                    String[] parts = existingFingerprints.split(",");
                    for (String part : parts) {
                        String fingerprint = part.trim();
                        if (!fingerprint.isEmpty() && !fingerprints.contains(fingerprint)) {
                            fingerprints.add(fingerprint);
                        }
                    }
                    line = "MyFamily " + String.join(", ", fingerprints);
                }
                fileContent.add(line);
            }

            // If MyFamily line does not exist, add it
            if (!myFamilyExists && !fingerprints.isEmpty()) {
                fileContent.add("MyFamily " + String.join(", ", fingerprints));
            }
        }

        // Write the updated content back to the torrc file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(torrcFilePath.toFile()))) {
            for (String line : fileContent) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
}
