package com.school.torconfigtool.service;

import com.school.torconfigtool.model.BridgeConfig;
import com.school.torconfigtool.model.TorrcFileCreator;
import com.school.torconfigtool.util.RelayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Service class for managing Tor bridges.
 */
@Service
public class BridgeService {
    private static final Logger logger = LoggerFactory.getLogger(BridgeService.class);
    private static final String TORRC_DIRECTORY_PATH = "torrc";
    private static final String TORRC_FILE_PREFIX = "torrc-";

    private final NginxService nginxService;
    private final WebtunnelService webtunnelService;
    private final RelayService relayService;
    private final RelayUtils relayUtils;

    /**
     * Constructor for the BridgeService.
     *
     * @param nginxService     The Nginx service.
     * @param webtunnelService The webtunnel service.
     * @param relayService     The relay service.
     */
    public BridgeService(NginxService nginxService, WebtunnelService webtunnelService, RelayService relayService, RelayUtils relayUtils) {
        this.nginxService = nginxService;
        this.webtunnelService = webtunnelService;
        this.relayService = relayService;
        this.relayUtils = relayUtils;
    }

    /**
     * Configures a Tor bridge relay.
     *
     * @param bridgeType Type of the bridge.
     * @param bridgePort Port of the bridge.
     * @param bridgeTransportListenAddr Transport listen address of the bridge.
     * @param bridgeContact Contact of the bridge.
     * @param bridgeNickname Nickname of the bridge.
     * @param webtunnelDomain Domain of the webtunnel.
     * @param bridgeControlPort Control port of the bridge.
     * @param webtunnelUrl URL of the webtunnel.
     * @param webtunnelPort Port of the webtunnel.
     * @param bridgeBandwidth Bandwidth of the bridge.
     * @param model Model for the view.
     * @throws Exception If an error occurs while configuring the bridge.
     */
    public void configureBridgeInternal(String bridgeType, Integer bridgePort, Integer bridgeTransportListenAddr, String bridgeContact, String bridgeNickname, String webtunnelDomain, int bridgeControlPort, String webtunnelUrl, Integer webtunnelPort, Integer bridgeBandwidth, Model model) throws Exception {
        String torrcFileName = TORRC_FILE_PREFIX + bridgeNickname + "_bridge";
        Path torrcFilePath = Paths.get(TORRC_DIRECTORY_PATH, torrcFileName).toAbsolutePath().normalize();

        if (RelayUtils.relayExists(bridgeNickname)) {
            model.addAttribute("errorMessage", "A relay with the same nickname already exists.");
            return;
        }

        // Log the bridgeType before creating the BridgeRelayConfig object
        logger.info("Bridge type before creating BridgeRelayConfig: " + bridgeType);

        BridgeConfig config = createBridgeConfig(bridgeTransportListenAddr, bridgeType, bridgeNickname, bridgePort, bridgeContact, bridgeControlPort, bridgeBandwidth, webtunnelDomain, webtunnelUrl, webtunnelPort);

        // Log the bridgeType after creating the BridgeRelayConfig object
        logger.info("Bridge type after creating BridgeRelayConfig: " + config.getBridgeType());

        if (!torrcFilePath.toFile().exists()) {
            TorrcFileCreator.createTorrcFile(torrcFilePath.toString(), config);
        }

        model.addAttribute("successMessage", "Tor Relay configured successfully!");

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
     * Creates a BridgeConfig object.
     *
     * @param bridgeTransportListenAddr Transport listen address of the bridge.
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

        // Log the bridgeType after setting it in the BridgeRelayConfig object
        logger.info("Bridge type after setting in BridgeRelayConfig: " + config.getBridgeType());

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
     * @param bridgeTransportListenAddr Transport listen address of the bridge.
     * @param bridgeContact Contact of the bridge.
     * @param bridgeNickname Nickname of the bridge.
     * @param webtunnelDomain Domain of the webtunnel.
     * @param bridgeControlPort Control port of the bridge.
     * @param webtunnelUrl URL of the webtunnel.
     * @param webtunnelPort Port of the webtunnel.
     * @param bridgeBandwidth Bandwidth of the bridge.
     * @param model Model for the view.
     */
    public void configureBridge(String bridgeType, Integer bridgePort, Integer bridgeTransportListenAddr, String bridgeContact, String bridgeNickname, String webtunnelDomain, int bridgeControlPort, String webtunnelUrl, Integer webtunnelPort, Integer bridgeBandwidth, Model model) {
        try {
            if (relayService.getBridgeCount() >= 2) {
                model.addAttribute("errorMessage", "You can only configure up to 2 bridges.");
                return;
            }

            Set<Integer> uniquePorts = new HashSet<>(Arrays.asList(bridgePort, bridgeTransportListenAddr, bridgeControlPort, webtunnelPort));
            if (uniquePorts.size() < 4) {
                model.addAttribute("errorMessage", "The ports must be unique.");
                return;
            }

            // Check if the ports are available
            if (bridgePort != null && !RelayUtils.isPortAvailable(bridgeNickname, bridgePort)) {
                model.addAttribute("errorMessage", "The bridge port is not available.");
                return;
            }
            if (bridgeTransportListenAddr != null && !RelayUtils.isPortAvailable(bridgeNickname, bridgeTransportListenAddr)) {
                model.addAttribute("errorMessage", "The bridge transport listen address port is not available.");
                return;
            }
            if (!RelayUtils.isPortAvailable(bridgeNickname, bridgeControlPort)) {
                model.addAttribute("errorMessage", "The bridge control port is not available.");
                return;
            }
            if (webtunnelPort != null && !RelayUtils.isPortAvailable(bridgeNickname, webtunnelPort)) {
                model.addAttribute("errorMessage", "The webtunnel port is not available.");
                return;
            }

            configureBridgeInternal(bridgeType, bridgePort, bridgeTransportListenAddr, bridgeContact, bridgeNickname, webtunnelDomain, bridgeControlPort, webtunnelUrl, webtunnelPort, bridgeBandwidth, model);
            model.addAttribute("successMessage", "Tor Relay configured successfully!");
        } catch (Exception e) {
            logger.error("Error during Tor Relay configuration", e);
            model.addAttribute("errorMessage", "Failed to configure Tor Relay. One or more ports are already in use.");
        }
    }

    /**
     * Checks if the bridge limit has been reached.
     *
     * @param bridgeType Type of the bridge.
     * @return Map containing the bridge limit reached status and the bridge count.
     */
    public Map<String, Object> checkBridgeLimit(String bridgeType) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Integer> bridgeCountByType = relayService.getBridgeCountByType();

        if (!RelayService.isLimitOn()) {
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
