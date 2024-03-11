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

    /**
     * Creates a BridgeConfig object.
     *
     * @param bridgeTransportListenAddr Transport listen to the address of the bridge.
     * @param bridgeType Type of the bridge.
     * @param bridgeNickname Nickname of the bridge.
     * @param bridgePort Port of the bridge.
     * @param bridgeContact Contact of the bridge.
     * @param bridgeControlPort Control port of the bridge.
     * @param bridgeBandwidth Bandwidth of the bridge.
     * @param webtunnelDomain Domain of the webtunnel.
     * @param webtunnelUrl URL of the webtunnel.
     * @param webtunnelPort Port of the webtunnel.
     * @return BridgeConfig object.
     */
    private BridgeConfig createBridgeConfig(Integer bridgeTransportListenAddr, String bridgeType, String bridgeNickname, Integer bridgePort, String bridgeContact, int bridgeControlPort, Integer bridgeBandwidth, String webtunnelDomain, String webtunnelUrl, Integer webtunnelPort) {
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

        return config;
    }

    /**
     * Configures a Tor bridge.
     *
     * @param bridgeType Type of the bridge.
     * @param bridgePort Port of the bridge.
     * @param bridgeTransportListenAddr Transport listen to the address of the bridge.
     * @param bridgeContact Contact of the bridge.
     * @param bridgeNickname Nickname of the bridge.
     * @param webtunnelDomain Domain of the webtunnel.
     * @param bridgeControlPort Control port of the bridge.
     * @param webtunnelUrl URL of the webtunnel.
     * @param webtunnelPort Port of the webtunnel.
     * @param bridgeBandwidth Bandwidth of the bridge.
     */
    public void configureBridge(String bridgeType, Integer bridgePort, Integer bridgeTransportListenAddr, String bridgeContact, String bridgeNickname, String webtunnelDomain, int bridgeControlPort, String webtunnelUrl, Integer webtunnelPort, Integer bridgeBandwidth) throws Exception {
        if (relayInformationService.getBridgeCount() >= 2) {
            throw new Exception("You can only configure up to 2 bridges.");
        }

        if (RelayUtilityService.relayExists(bridgeNickname)) {
            throw new Exception("A relay with the same nickname already exists.");
        }

        // Check if the ports are available
        if (webtunnelPort != null) {
            if (!RelayUtilityService.portsAreAvailable(bridgeNickname, bridgePort, bridgeTransportListenAddr, bridgeControlPort, webtunnelPort)) {
                throw new Exception("One or more ports are already in use.");
            }
        }
        else if (bridgePort == null && bridgeTransportListenAddr == null) {
            if (!RelayUtilityService.portsAreAvailable(bridgeNickname, bridgeControlPort)) {
                throw new Exception("One or more ports are already in use.");
            }
        } else {
            if (!RelayUtilityService.portsAreAvailable(bridgeNickname, bridgePort, bridgeTransportListenAddr, bridgeControlPort)) {
                throw new Exception("One or more ports are already in use.");
            }
        }

        String torrcFileName = TORRC_FILE_PREFIX + bridgeNickname + "_bridge";
        Path torrcFilePath = Paths.get(TORRC_DIRECTORY_PATH, torrcFileName).toAbsolutePath().normalize();

        BridgeConfig config = createBridgeConfig(bridgeTransportListenAddr, bridgeType, bridgeNickname, bridgePort, bridgeContact, bridgeControlPort, bridgeBandwidth, webtunnelDomain, webtunnelUrl, webtunnelPort);

        if (!torrcFilePath.toFile().exists()) {
            TorrcFileCreator.createTorrcFile(torrcFilePath.toString(), config);
        }

        if (webtunnelUrl != null && !webtunnelUrl.isEmpty()) {
            nginxService.generateNginxConfig();
            nginxService.changeRootDirectory(System.getProperty("user.dir") + "/onion/www/service-80");
            webtunnelService.setupWebtunnel(webtunnelUrl);
            String randomString = UUID.randomUUID().toString().replace("-", "").substring(0, 24);
            nginxService.modifyNginxDefaultConfig(System.getProperty("user.dir"), randomString, webtunnelUrl);
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
    public Map<String, Object> checkBridgeLimit(String bridgeType) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Integer> bridgeCountByType = relayInformationService.getBridgeCountByType();

        if (!RelayInformationService.isLimitOn()) {
            response.put("bridgeLimitReached", false);
            response.put("bridgeCount", bridgeCountByType.get(bridgeType));
            return response;
        }

        switch (bridgeType) {
            case "obfs4":
                response.put("bridgeLimitReached", bridgeCountByType.get("obfs4") >= 2);
                response.put("bridgeCount", bridgeCountByType.get("obfs4"));
                break;
            case "webtunnel":
                response.put("bridgeLimitReached", bridgeCountByType.get("webtunnel") >= 1);
                response.put("bridgeCount", bridgeCountByType.get("webtunnel"));
                break;
            case "snowflake":
                response.put("bridgeLimitReached", bridgeCountByType.get("snowflake") >= 1);
                response.put("bridgeCount", bridgeCountByType.get("snowflake"));
                break;
            default:
                response.put("bridgeLimitReached", false);
                response.put("bridgeCount", 0);
        }

        return response;
    }
}
