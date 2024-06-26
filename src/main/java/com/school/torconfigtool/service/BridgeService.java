package com.school.torconfigtool.service;

import com.school.torconfigtool.model.BridgeConfig;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.school.torconfigtool.util.Constants.TORRC_FILE_PREFIX;

/**
 * Service class for managing Tor bridges.
 */
@Service
public class BridgeService {
    private static final String TORRC_DIRECTORY_PATH = "torrc";

    private final NginxService nginxService;
    private final WebtunnelService webtunnelService;
    private final RelayInformationService relayInformationService;

    /**
     * Constructor for the BridgeService.
     *
     * @param nginxService     The Nginx service.
     * @param webtunnelService The webtunnel service.
     * @param relayInformationService     The relay service.
     */
    public BridgeService(NginxService nginxService, WebtunnelService webtunnelService, RelayInformationService relayInformationService) {
        this.nginxService = nginxService;
        this.webtunnelService = webtunnelService;
        this.relayInformationService = relayInformationService;
    }

    public void configureBridge(String bridgeType, Integer bridgePort, Integer bridgeTransportListenAddr, String bridgeContact, String bridgeNickname, String webtunnelDomain, int bridgeControlPort, String webtunnelUrl, Integer webtunnelPort, Integer bridgeBandwidth) throws Exception {

        if (RelayUtilityService.relayExists(bridgeNickname)) {
            throw new Exception("A relay with the same nickname already exists.");
        }

        if (bridgePort == null) {
            if (!RelayUtilityService.portsAreAvailable(bridgeNickname, bridgeControlPort, bridgeTransportListenAddr)) {
                throw new Exception("One or more ports are already in use.");
            }
        } else {
            if (!RelayUtilityService.portsAreAvailable(bridgeNickname, bridgePort, bridgeTransportListenAddr, bridgeControlPort)) {
                throw new Exception("One or more ports are already in use.");
            }
        }

        String torrcFileName = TORRC_FILE_PREFIX + bridgeNickname + "_bridge";
        Path torrcFilePath = Paths.get(TORRC_DIRECTORY_PATH, torrcFileName).toAbsolutePath().normalize();

        // Create BridgeConfig directly in the configureBridge method
        BridgeConfig config = new BridgeConfig();
        config.setBridgeType(bridgeType);
        config.setNickname(bridgeNickname);
        if (bridgePort != null)
            config.setOrPort(String.valueOf(bridgePort));
        config.setContact(bridgeContact);
        config.setControlPort(String.valueOf(bridgeControlPort));
        if (bridgeBandwidth != null)
            config.setBandwidthRate(String.valueOf(bridgeBandwidth));
        if (webtunnelDomain != null)
            config.setWebtunnelDomain(webtunnelDomain);
        if (webtunnelUrl != null)
            config.setWebtunnelUrl(webtunnelUrl);
        if (webtunnelPort != null)
            config.setWebtunnelPort(webtunnelPort);
        if (bridgeTransportListenAddr != null)
            config.setServerTransport(String.valueOf(bridgeTransportListenAddr));

        if (!torrcFilePath.toFile().exists()) {
            TorrcFileCreator.createTorrcFile(torrcFilePath.toString(), config);
        }

        if (webtunnelUrl != null && !webtunnelUrl.isEmpty() && webtunnelPort != null && bridgeTransportListenAddr != null) {
            nginxService.configureNginx(webtunnelPort);
            webtunnelService.setupWebtunnel(webtunnelUrl, webtunnelPort);
            String randomString = UUID.randomUUID().toString().replace("-", "").substring(0, 24);
            nginxService.configureNginxServerForWebtunnel(System.getProperty("user.dir"), randomString, webtunnelUrl, webtunnelPort, bridgeTransportListenAddr);
            config.setPath(randomString);
            webtunnelService.updateTorrcFile(config);

            nginxService.reloadNginx();
        }
    }

    /**
     * Checks if the bridge limit has been reached.
     *
     * @param bridgeType Type of the bridge.
     * @return Map containing the bridge limit reached status and the bridge counts.
     */
    public Map<String, Object> countBridges(String bridgeType) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Integer> bridgeCountByType = relayInformationService.getBridgeCountByType();

        switch (bridgeType) {
            case "obfs4":
                response.put("bridgeCount", bridgeCountByType.get("obfs4"));
                break;
            case "webtunnel":
                response.put("bridgeCount", bridgeCountByType.get("webtunnel"));
                break;
            case "snowflake":
                response.put("bridgeCount", bridgeCountByType.get("snowflake"));
                break;
            default:
                response.put("bridgeCount", 0);
        }

        return response;
    }

    /**
     * Checks if a Bridge has been configured.
     *
     * @return A map containing the result.
     */
    public Map<String, Boolean> checkBridgeConfigured() {
        Map<String, Boolean> response = new HashMap<>();
        response.put("bridgeConfigured", relayInformationService.getBridgeCount() > 0);
        return response;
    }
}
