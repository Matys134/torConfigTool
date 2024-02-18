package com.school.torconfigtool;

import com.school.torconfigtool.model.GuardConfig;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.nio.file.Path;

@Service
public class GuardService {

    private final RelayService relayService;

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

    public void configureGuard(String relayNickname, int relayPort, String relayContact, int controlPort, Integer relayBandwidth, Path torrcFilePath, Model model) throws Exception {
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
    }
}
