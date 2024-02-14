package com.school.torconfigtool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class BridgeSetupService {

    private final RelayService relayService;
    private final WebtunnelSetupService webtunnelSetupService;

    private static final String TORRC_DIRECTORY_PATH = "torrc";
    private static final String TORRC_FILE_PREFIX = "torrc-";

    @Autowired
    public BridgeSetupService(RelayService relayService, WebtunnelSetupService webtunnelSetupService) {
        this.relayService = relayService;
        this.webtunnelSetupService = webtunnelSetupService;
    }

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

    private void createTorrcFile(Path torrcFilePath, BridgeRelayConfig config) {
        if (!torrcFilePath.toFile().exists()) {
            TorrcFileCreator.createTorrcFile(torrcFilePath.toString(), config);
        }
    }

    public void configureBridge(String bridgeType, Integer bridgePort, Integer bridgeTransportListenAddr, String bridgeContact, String bridgeNickname, String webtunnelDomain, int bridgeControlPort, String webtunnelUrl, Integer webtunnelPort, Integer bridgeBandwidth, Model model) throws Exception {
        if (relayService.getBridgeCount() >= 2) {
            model.addAttribute("errorMessage", "You can only configure up to 2 bridges.");
            throw new Exception("Bridge limit reached");
        }
        configureBridgeInternal(bridgeType, bridgePort, bridgeTransportListenAddr, bridgeContact, bridgeNickname, webtunnelDomain, bridgeControlPort, webtunnelUrl, webtunnelPort, bridgeBandwidth, model);
    }
}
