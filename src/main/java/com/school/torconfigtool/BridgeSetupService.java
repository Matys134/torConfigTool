package com.school.torconfigtool;

import com.school.torconfigtool.RelayService;
import com.school.torconfigtool.RelayUtils;
import com.school.torconfigtool.TorrcFileCreator;
import com.school.torconfigtool.WebtunnelSetupService;
import com.school.torconfigtool.BridgeRelayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service class for setting up a bridge.
 */
@Service
public class BridgeSetupService {

    private final RelayService relayService;
    private final WebtunnelSetupService webtunnelSetupService;

    private static final String TORRC_DIRECTORY_PATH = "torrc";
    private static final String TORRC_FILE_PREFIX = "torrc-";

    /**
     * Constructor for BridgeSetupService.
     *
     * @param relayService           The relay service to be used.
     * @param webtunnelSetupService  The webtunnel setup service to be used.
     */
    @Autowired
    public BridgeSetupService(RelayService relayService, WebtunnelSetupService webtunnelSetupService) {
        this.relayService = relayService;
        this.webtunnelSetupService = webtunnelSetupService;
    }

    /**
     * Configures the bridge internally.
     *
     * @param bridgeType                 The type of the bridge.
     * @param bridgePort                 The port of the bridge.
     * @param bridgeTransportListenAddr  The transport listen address of the bridge.
     * @param bridgeContact              The contact of the bridge.
     * @param bridgeNickname             The nickname of the bridge.
     * @param webtunnelDomain            The domain of the webtunnel.
     * @param bridgeControlPort          The control port of the bridge.
     * @param webtunnelUrl               The URL of the webtunnel.
     * @param webtunnelPort              The port of the webtunnel.
     * @param bridgeBandwidth            The bandwidth of the bridge.
     * @param model                      The model to be used.
     * @throws Exception                 If an error occurs during the configuration.
     */
    public void configureBridgeInternal(String bridgeType, Integer bridgePort, Integer bridgeTransportListenAddr, String bridgeContact, String bridgeNickname, String webtunnelDomain, int bridgeControlPort, String webtunnelUrl, Integer webtunnelPort, Integer bridgeBandwidth, Model model) throws Exception {
        String torrcFileName = TORRC_FILE_PREFIX + bridgeNickname + "_bridge";
        Path torrcFilePath = Paths.get(TORRC_DIRECTORY_PATH, torrcFileName).toAbsolutePath().normalize();

        if (RelayUtils.relayExists(bridgeNickname)) {
            model.addAttribute("errorMessage", "A relay with the same nickname already exists.");
            return;
        }

        BridgeRelayConfig config = BridgeRelayConfig.create(bridgeTransportListenAddr, bridgeType, bridgeNickname, bridgePort, bridgeContact, bridgeControlPort, bridgeBandwidth, webtunnelDomain, webtunnelUrl, webtunnelPort);

        createTorrcFile(torrcFilePath, config);

        model.addAttribute("successMessage", "Tor Relay configured successfully!");

        webtunnelSetupService.setupWebtunnel(webtunnelUrl, config);
    }

    /**
     * Creates a torrc file if it does not exist.
     *
     * @param torrcFilePath  The path of the torrc file.
     * @param config         The configuration to be used.
     */
    private void createTorrcFile(Path torrcFilePath, BridgeRelayConfig config) {
        if (!torrcFilePath.toFile().exists()) {
            TorrcFileCreator.createTorrcFile(torrcFilePath.toString(), config);
        }
    }

    /**
     * Configures the bridge.
     *
     * @param bridgeType                 The type of the bridge.
     * @param bridgePort                 The port of the bridge.
     * @param bridgeTransportListenAddr  The transport listen address of the bridge.
     * @param bridgeContact              The contact of the bridge.
     * @param bridgeNickname             The nickname of the bridge.
     * @param webtunnelDomain            The domain of the webtunnel.
     * @param bridgeControlPort          The control port of the bridge.
     * @param webtunnelUrl               The URL of the webtunnel.
     * @param webtunnelPort              The port of the webtunnel.
     * @param bridgeBandwidth            The bandwidth of the bridge.
     * @param model                      The model to be used.
     * @throws Exception                 If an error occurs during the configuration.
     */
    public void configureBridge(String bridgeType, Integer bridgePort, Integer bridgeTransportListenAddr, String bridgeContact, String bridgeNickname, String webtunnelDomain, int bridgeControlPort, String webtunnelUrl, Integer webtunnelPort, Integer bridgeBandwidth, Model model) throws Exception {
        if (relayService.getBridgeCount() >= 2) {
            model.addAttribute("errorMessage", "You can only configure up to 2 bridges.");
            throw new Exception("Bridge limit reached");
        }
        configureBridgeInternal(bridgeType, bridgePort, bridgeTransportListenAddr, bridgeContact, bridgeNickname, webtunnelDomain, bridgeControlPort, webtunnelUrl, webtunnelPort, bridgeBandwidth, model);
    }
}