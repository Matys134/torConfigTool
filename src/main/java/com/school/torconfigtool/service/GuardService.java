package com.school.torconfigtool.service;

import com.school.torconfigtool.model.GuardConfig;
import com.school.torconfigtool.util.Constants;
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

    // Directory path for torrc files


    /**
     * Constructor for GuardService
     *
     * @param relayInformationService The service to be used for relay operations.
     */
    public GuardService(RelayInformationService relayInformationService) {
        this.relayInformationService = relayInformationService;
    }

    /**
     * Configures a Guard relay with the provided parameters.
     *
     * @param relayNickname   The nickname of the relay.
     * @param relayPort       The port of the relay.
     * @param relayContact    The contact information for the relay.
     * @param controlPort     The control port for the relay.
     * @param relayBandwidth  The bandwidth for the guard. This is optional.
     * @throws Exception if the relay already exists, or if the ports are already in use.
     */
    public void configureGuard(String relayNickname, int relayPort, String relayContact, int controlPort, Integer relayBandwidth) throws Exception {
        String torrcFileName = TORRC_FILE_PREFIX + relayNickname + "_guard";
        Path torrcFilePath = Paths.get(TORRC_DIRECTORY_PATH, torrcFileName).toAbsolutePath().normalize();

        if (RelayUtilityService.relayExists(relayNickname)) {
            throw new Exception("A relay with the same nickname already exists.");
        }

        if (!RelayUtilityService.portsAreAvailable(relayNickname, relayPort, controlPort)) {
            throw new Exception("One or more ports are already in use.");
        }

        GuardConfig config = new GuardConfig();
        config.setNickname(relayNickname);
        config.setOrPort(String.valueOf(relayPort));
        config.setContact(relayContact);
        config.setControlPort(String.valueOf(controlPort));
        config.setBandwidthRate(String.valueOf(relayBandwidth));

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

        response.put("guardLimitReached", guardCount >= Constants.MAX_GUARD_COUNT);
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