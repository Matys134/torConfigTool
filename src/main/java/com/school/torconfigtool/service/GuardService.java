package com.school.torconfigtool.service;

import com.school.torconfigtool.util.RelayUtils;
import com.school.torconfigtool.TorrcFileCreator;
import com.school.torconfigtool.model.GuardConfig;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * This service class is responsible for handling operations related to Guard Relays.
 */
@Service
public class GuardService {

    // RelayService instance for relay operations
    private final RelayService relayService;

    // Directory path for torrc files
    private static final String TORRC_DIRECTORY_PATH = "torrc/";

    // Prefix for torrc file names
    private static final String TORRC_FILE_PREFIX = "torrc-";

    /**
     * Constructor for GuardService
     *
     * @param relayService The service to be used for relay operations.
     */
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

    /**
     * Configures a Guard Relay with the given parameters.
     *
     * @param relayNickname   The nickname of the Guard Relay.
     * @param relayPort       The OR port of the Guard Relay.
     * @param relayContact    The contact information of the Guard Relay.
     * @param controlPort     The control port of the Guard Relay.
     * @param relayBandwidth  The bandwidth of the Guard Relay.
     * @throws Exception If the ports are not available or a relay with the same nickname already exists.
     */
    public void configureGuard(String relayNickname, int relayPort, String relayContact, int controlPort, Integer relayBandwidth) throws Exception {
        String torrcFileName = TORRC_FILE_PREFIX + relayNickname + "_guard";
        Path torrcFilePath = Paths.get(TORRC_DIRECTORY_PATH, torrcFileName).toAbsolutePath().normalize();

        if (!relayService.arePortsAvailable(relayNickname, relayPort, controlPort)) {
            throw new Exception("One or more ports are already in use.");
        }

        if (RelayUtils.relayExists(relayNickname)) {
            throw new Exception("A relay with the same nickname already exists.");
        }

        GuardConfig config = createGuardConfig(relayNickname, relayPort, relayContact, controlPort, relayBandwidth);
        if (!torrcFilePath.toFile().exists()) {
            TorrcFileCreator.createTorrcFile(torrcFilePath.toString(), config);
        }
    }

    /**
     * Checks if the Guard limit has been reached.
     *
     * @return A map containing the result and the current Guard count.
     */
    public Map<String, Object> checkGuardLimit() {
        Map<String, Object> response = new HashMap<>();
        int guardCount = relayService.getGuardCount();

        if (!RelayService.isLimitOn()) {
            response.put("guardLimitReached", false);
            response.put("guardCount", guardCount);
            return response;
        }

        response.put("guardLimitReached", guardCount >= 8);
        response.put("guardCount", guardCount);
        return response;
    }

    /**
     * Checks if a Bridge has been configured.
     *
     * @return A map containing the result.
     */
    public Map<String, Boolean> checkBridgeConfigured() {
        Map<String, Boolean> response = new HashMap<>();
        response.put("bridgeConfigured", relayService.getBridgeCount() > 0);
        return response;
    }

    /**
     * Gets the limit state and Guard count.
     *
     * @return A map containing the limit state and Guard count.
     */
    public Map<String, Object> getLimitStateAndGuardCount() {
        Map<String, Object> response = new HashMap<>();
        response.put("limitOn", RelayService.isLimitOn());
        response.put("guardCount", relayService.getGuardCount());
        return response;
    }

    /**
     * Checks if a Guard has been configured.
     *
     * @return A map containing the result.
     */
    public Map<String, Boolean> checkGuardConfigured() {
        Map<String, Boolean> response = new HashMap<>();
        boolean isGuardConfigured = relayService.getGuardCount() > 0;
        response.put("guardConfigured", isGuardConfigured);
        return response;
    }
}