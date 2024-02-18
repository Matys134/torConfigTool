package com.school.torconfigtool;

import com.school.torconfigtool.model.GuardConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class GuardService {

    private final RelayService relayService;
    private static final String TORRC_DIRECTORY_PATH = "torrc/";

    private static final Logger logger = LoggerFactory.getLogger(GuardService.class);

    public GuardService(RelayService relayService) {
        this.relayService = relayService;
    }

    /**
     * Creates a new GuardRelayConfig object with the given parameters.
     *
     * @param relayNickname   The nickname of the Guard Relay.
     * @param relayPort       The OR port of the Guard Relay.
     * @param relayContact    The contact information of the Guard Relay.
     * @param controlPort     The control port of the Guard Relay.
     * @param relayBandwidth  The bandwidth of the Guard Relay.
     * @return A new GuardRelayConfig object with the given parameters.
     */
    public GuardConfig createGuardConfig(String relayNickname, int relayPort, String relayContact, int controlPort, Integer relayBandwidth) {
        GuardConfig config = new GuardConfig();
        config.setNickname(relayNickname);
        config.setOrPort(String.valueOf(relayPort));
        config.setContact(relayContact);
        config.setControlPort(String.valueOf(controlPort));
        if (relayBandwidth != null) {
            config.setBandwidthRate(String.valueOf(relayBandwidth));
        }

        return config;
    }

    // In GuardService.java
    public void configureGuard(String relayNickname, int relayPort, String relayContact, int controlPort, Integer relayBandwidth, String torrcFileName, Model model) {
        try {
            Path torrcFilePath = Paths.get(TORRC_DIRECTORY_PATH, torrcFileName).toAbsolutePath().normalize();

            if (!relayService.arePortsAvailable(relayNickname, relayPort, controlPort)) {
                model.addAttribute("errorMessage", "One or more ports are already in use.");
                throw new Exception("One or more ports are already in use.");
            }

            if (RelayUtils.relayExists(relayNickname)) {
                model.addAttribute("errorMessage", "A relay with the same nickname already exists.");
                throw new Exception("A relay with the same nickname already exists.");
            }

            GuardConfig config = createGuardConfig(relayNickname, relayPort, relayContact, controlPort, relayBandwidth);
            if (!torrcFilePath.toFile().exists()) {
                TorrcFileCreator.createTorrcFile(torrcFilePath.toString(), config);
            }

            model.addAttribute("successMessage", "Tor Relay configured successfully!");
        } catch (Exception e) {
            logger.error("Error during Tor Relay configuration", e);
            model.addAttribute("errorMessage", "Failed to configure Tor Relay.");
        }
    }
}
