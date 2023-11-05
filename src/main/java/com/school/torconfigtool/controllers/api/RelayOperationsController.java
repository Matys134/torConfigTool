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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        String command = (start ? "tor -f " : "kill -SIGINT ") + torrcFilePath.toAbsolutePath();
        int exitCode = processManagementService.executeCommand(command);
        if (exitCode != 0) {
            throw new RelayOperationException("Failed to " + (start ? "start" : "stop") + " Tor Relay service.");
        }
    }

    private Path buildTorrcFilePath(String relayNickname, String relayType) {
        String folder = relayType.equals("guard") ? "guard" : "bridge";
        return Paths.get(System.getProperty("user.dir"), "torrc", folder, "local-torrc-" + relayNickname);
    }
}
