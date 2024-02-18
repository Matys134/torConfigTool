package com.school.torconfigtool;

import com.simtechdata.waifupnp.UPnP;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/relay-operations")
public class RelayOperationsController {

    private static final Logger logger = LoggerFactory.getLogger(RelayOperationsController.class);
    private final TorConfigurationService torConfigurationService;
    private final RelayOperationsService relayOperationsService;

    public RelayOperationsController(TorConfigurationService torConfigurationService,
                                     RelayOperationsService relayOperationsService) {
        this.torConfigurationService = torConfigurationService;
        this.relayOperationsService = relayOperationsService;

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
        relayOperationsService.relayOperations(model);
        return "relay-operations";
    }


    @PostMapping("/stop")
    public String stopRelay(@RequestParam String relayNickname, @RequestParam String relayType, Model model) {
        String view = relayOperationsService.changeRelayState(relayNickname, relayType, model, false);

        new Thread(() -> {
            try {
                relayOperationsService.waitForStatusChange(relayNickname, relayType, "offline");
                // Close the ORPort after the relay has stopped
                relayOperationsService.closeOrPort(relayNickname, relayType);
            } catch (InterruptedException e) {
                logger.error("Error while waiting for relay to stop", e);
            }
        }).start();

        return view;
    }

    @PostMapping("/start")
    public String startRelay(@RequestParam String relayNickname, @RequestParam String relayType, Model model) {
        String view;
        if ("guard".equals(relayType)) {
            view = relayOperationsService.changeRelayState(relayNickname, relayType, model, true);
        } else {
            view = relayOperationsService.changeRelayStateWithoutFingerprint(relayNickname, relayType, model);
        }
        System.out.println("Relay state changed");

        new Thread(() -> {
            try {
                relayOperationsService.waitForStatusChange(relayNickname, relayType, "online");
                openOrPort(relayNickname, relayType);
            } catch (InterruptedException e) {
                logger.error("Error while waiting for relay to start", e);
            }
        }).start();
        System.out.println("Returning view");

        return view;
    }

    @GetMapping("/status")
    @ResponseBody
    public String getRelayStatus(@RequestParam String relayNickname, @RequestParam String relayType) {
        return relayOperationsService.getRelayStatus(relayNickname, relayType);
    }

    @PostMapping("/remove")
    @ResponseBody
    public Map<String, Object> removeRelay(@RequestParam String relayNickname, @RequestParam String relayType) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Build paths for Torrc file and DataDirectory
            Path torrcFilePath = relayOperationsService.buildTorrcFilePath(relayNickname, relayType);
            String dataDirectoryPath = relayOperationsService.buildDataDirectoryPath(relayNickname);

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

            relayOperationsService.reloadNginx();

            response.put("success", true);
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to remove Torrc file, DataDirectory, Onion files, Nginx configuration file and symbolic link for relayNickname: {}", relayNickname, e);
            response.put("success", false);
        }
        return response;
    }

    // Method for opening orport of a relay using UPnP
    @PostMapping("/open-orport")
    @ResponseBody
    public Map<String, Object> openOrPort(@RequestParam String relayNickname, @RequestParam String relayType) {
        Map<String, Object> response = new HashMap<>();
        // Build the path to the torrc file
        Path torrcFilePath = relayOperationsService.buildTorrcFilePath(relayNickname, relayType);

        // Get the orport from the torrc file
        int orPort = relayOperationsService.getOrPort(torrcFilePath);

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

    @PostMapping("/toggle-upnp")
    @ResponseBody
    public Map<String, Object> toggleUPnP(@RequestParam boolean enable) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get the list of all guard relays
            List<TorConfiguration> guardConfigs = torConfigurationService.readTorConfigurationsFromFolder(torConfigurationService.buildFolderPath(), "guard");
            for (TorConfiguration config : guardConfigs) {
                if (enable) {
                    String status = getRelayStatus(config.getGuardConfig().getNickname(), "guard");
                    if ("online".equals(status)) {
                        // Open the ORPort
                        openOrPort(config.getGuardConfig().getNickname(), "guard");
                    }
                } else {
                    // Close the ORPort
                    relayOperationsService.closeOrPort(config.getGuardConfig().getNickname(), "guard");
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
}
