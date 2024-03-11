package com.school.torconfigtool.service;

import com.school.torconfigtool.model.GuardConfig;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.school.torconfigtool.util.Constants.TORRC_DIRECTORY_PATH;
import static com.school.torconfigtool.util.Constants.TORRC_FILE_PREFIX;

/**
 * This service class is responsible for handling operations related to Guard Relays.
 */
@Service
public class GuardService {

    // RelayService instance for relay operations
    private final RelayInformationService relayInformationService;
    private final RelayUtilityService relayUtilityService;

    // Directory path for torrc files


    /**
     * Constructor for GuardService
     *
     * @param relayInformationService The service to be used for relay operations.
     */
    public GuardService(RelayInformationService relayInformationService, RelayUtilityService relayUtilityService) {
        this.relayInformationService = relayInformationService;
        this.relayUtilityService = relayUtilityService;
    }

    /**
     * Creates a new GuardRelayConfig object with the given parameters.
     *
     * @param relayNickname   The nickname of the Guard Relay.
     * @param relayPort       The OR port of the Guard Relay.
     * @param relayContact    The contact information of the Guard Relay.
     * @param controlPort     The control port of the Guard Relay.
     * @param guardBandwidth  The bandwidth of the Guard Relay.
     * @return A new GuardRelayConfig object with the given parameters.
     */
    private GuardConfig createGuardConfig(String relayNickname, int relayPort, String relayContact, int controlPort, Integer guardBandwidth) {
        GuardConfig config = new GuardConfig();
        config.setNickname(relayNickname);
        config.setOrPort(String.valueOf(relayPort));
        config.setContact(relayContact);
        config.setControlPort(String.valueOf(controlPort));
        if (guardBandwidth != null) {
            config.setBandwidthRate(String.valueOf(guardBandwidth));
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

        if (RelayUtilityService.relayExists(relayNickname)) {
            throw new Exception("A relay with the same nickname already exists.");
        }

        if (!relayUtilityService.arePortsAvailable(relayNickname, relayPort, controlPort)) {
            throw new Exception("One or more ports are already in use.");
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
        int guardCount = relayInformationService.getGuardCount();

        if (!RelayInformationService.isLimitOn()) {
            response.put("guardLimitReached", false);
            response.put("guardCount", guardCount);
            return response;
        }

        response.put("guardLimitReached", guardCount >= 8);
        response.put("guardCount", guardCount);
        return response;
    }

    /**
     * Checks if a Guard has been configured.
     *
     * @return A map containing the result.
     */
    public Map<String, Boolean> checkGuardConfigured() {
        Map<String, Boolean> response = new HashMap<>();
        boolean isGuardConfigured = relayInformationService.getGuardCount() > 0;
        response.put("guardConfigured", isGuardConfigured);
        return response;
    }
}