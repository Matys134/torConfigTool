package com.school.torconfigtool.controllers.api;

import com.school.torconfigtool.models.TorConfiguration;
import com.school.torconfigtool.service.ProcessManagementService;
import com.school.torconfigtool.service.TorConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@Controller
@RequestMapping("/relay-operations")
public class RelayOperationsController {

    private static final Logger logger = LoggerFactory.getLogger(RelayOperationsController.class);
    private final TorConfigurationService torConfigurationService;
    private final ProcessManagementService processManagementService;

    @Autowired
    public RelayOperationsController(TorConfigurationService torConfigurationService,
                                     ProcessManagementService processManagementService) {
        this.torConfigurationService = torConfigurationService;
        this.processManagementService = processManagementService;
    }

    @GetMapping
    public String relayOperations(Model model) {
        List<TorConfiguration> guardConfigs = torConfigurationService.readTorConfigurations("guard");
        List<TorConfiguration> bridgeConfigs = torConfigurationService.readTorConfigurations("bridge");

        model.addAttribute("guardConfigs", guardConfigs);
        model.addAttribute("bridgeConfigs", bridgeConfigs);

        return "relay-operations"; // Thymeleaf template name
    }

    @PostMapping("/stop")
    public String stopRelay(@RequestParam String relayNickname, @RequestParam String relayType, Model model) {
        String torrcFilePath = buildTorrcFilePath(relayNickname, relayType);
        try {
            int pid = processManagementService.getTorRelayPID(torrcFilePath);
            if (pid > 0) {
                String stopCommand = "kill -SIGINT " + pid;
                int exitCode = processManagementService.executeCommand(stopCommand);
                if (exitCode == 0) {
                    model.addAttribute("successMessage", "Tor Relay stopped successfully!");
                } else {
                    model.addAttribute("errorMessage", "Failed to stop Tor Relay service.");
                    logger.error("Failed to stop Tor Relay for relayNickname: {}", relayNickname);
                }
            } else {
                model.addAttribute("errorMessage", "Tor Relay is not running.");
            }
        } catch (Exception e) {
            logger.error("Failed to stop Tor Relay for relayNickname: {}", relayNickname, e);
            model.addAttribute("errorMessage", "Failed to stop Tor Relay.");
        }
        return relayOperations(model);
    }

    @PostMapping("/start")
    public String startRelay(@RequestParam String relayNickname, @RequestParam String relayType, Model model) {
        String torrcFilePath = buildTorrcFilePath(relayNickname, relayType);
        File torrcFile = new File(torrcFilePath);
        if (torrcFile.exists()) {
            String command = "tor -f " + torrcFile.getAbsolutePath();
            try {
                int exitCode = processManagementService.executeCommand(command);
                if (exitCode == 0) {
                    model.addAttribute("successMessage", "Tor Relay started successfully!");
                } else {
                    model.addAttribute("errorMessage", "Failed to start Tor Relay service.");
                    logger.error("Failed to start Tor Relay for relayNickname: {}", relayNickname);
                }
            } catch (Exception e) {
                logger.error("Failed to start Tor Relay for relayNickname: {}", relayNickname, e);
                model.addAttribute("errorMessage", "Failed to start Tor Relay.");
            }
        } else {
            model.addAttribute("errorMessage", "Torrc file does not exist for relay: " + relayNickname);
        }
        return relayOperations(model);
    }

    @GetMapping("/status")
    @ResponseBody
    public String getRelayStatus(@RequestParam String relayNickname, @RequestParam String relayType) {
        String torrcFilePath = buildTorrcFilePath(relayNickname, relayType);
        System.out.println(torrcFilePath);
        int pid = processManagementService.getTorRelayPID(torrcFilePath);
        if (pid > 0) {
            return "online";
        } else if (pid == -1) {
            return "offline";
        } else {
            return "error"; // Handle other cases as needed
        }
    }

    private String buildTorrcFilePath(String relayNickname, String relayType) {
        // You can extract this method to a utility class if it's used elsewhere as well
        String currentDirectory = System.getProperty("user.dir");
        String torrcFileName = "local-torrc-" + relayNickname;
        String folder = relayType.equals("guard") ? "guard" : "bridge";
        return currentDirectory + File.separator + "torrc" + File.separator + folder + File.separator + torrcFileName;
    }

    public class RelayOperationException extends RuntimeException {
        public RelayOperationException(String message) {
            super(message);
        }
    }
}
