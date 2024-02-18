package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Service class for managing Tor bridges.
 */
@Service
public class BridgeService {
    private static final Logger logger = LoggerFactory.getLogger(BridgeService.class);
    private static final String TORRC_DIRECTORY_PATH = "torrc";
    private static final String TORRC_FILE_PREFIX = "torrc-";

    private final RelayOperationsController relayOperationController;
    private final NginxService nginxService;
    private final WebtunnelService webtunnelService;

    /**
     * Constructor for BridgeService.
     *
     * @param relayOperationController Controller for relay operations.
     * @param nginxService Service for managing Nginx.
     * @param webtunnelService Service for managing webtunnel.
     */
    public BridgeService(RelayOperationsController relayOperationController, NginxService nginxService, WebtunnelService webtunnelService) {
        this.relayOperationController = relayOperationController;
        this.nginxService = nginxService;
        this.webtunnelService = webtunnelService;
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
     * @param startBridgeAfterConfig Whether to start the bridge after configuring it.
     * @param bridgeBandwidth Bandwidth of the bridge.
     * @param model Model for the view.
     * @throws Exception If an error occurs while configuring the bridge.
     */
    public void configureBridgeInternal(String bridgeType, Integer bridgePort, Integer bridgeTransportListenAddr, String bridgeContact, String bridgeNickname, String webtunnelDomain, int bridgeControlPort, String webtunnelUrl, Integer webtunnelPort, boolean startBridgeAfterConfig, Integer bridgeBandwidth, Model model) throws Exception {
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

        if (startBridgeAfterConfig) {
            try {
                relayOperationController.startRelay(bridgeNickname, "bridge", model);
                model.addAttribute("successMessage", "Tor Relay configured and started successfully!");
            } catch (Exception e) {
                logger.error("Error starting Tor Relay", e);
                model.addAttribute("errorMessage", "Failed to start Tor Relay.");
            }
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
}
