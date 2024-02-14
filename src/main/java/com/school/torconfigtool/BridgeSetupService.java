package com.school.torconfigtool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class BridgeSetupService {

    private final RelayService relayService;
    private final NginxService nginxService;

    private static final String TORRC_DIRECTORY_PATH = "torrc";
    private static final String TORRC_FILE_PREFIX = "torrc-";

    @Autowired
    public BridgeSetupService(RelayService relayService, NginxService nginxService) {
        this.relayService = relayService;
        this.nginxService = nginxService;
    }

    public void configureBridgeInternal(String bridgeType, Integer bridgePort, Integer bridgeTransportListenAddr, String bridgeContact, String bridgeNickname, String webtunnelDomain, int bridgeControlPort, String webtunnelUrl, Integer webtunnelPort, Integer bridgeBandwidth, Model model) throws Exception {
        String torrcFileName = TORRC_FILE_PREFIX + bridgeNickname + "_bridge";
        Path torrcFilePath = Paths.get(TORRC_DIRECTORY_PATH, torrcFileName).toAbsolutePath().normalize();

        if (RelayUtils.relayExists(bridgeNickname)) {
            model.addAttribute("errorMessage", "A relay with the same nickname already exists.");
            return;
        }

        BridgeRelayConfig config = createBridgeConfig(bridgeTransportListenAddr, bridgeType, bridgeNickname, bridgePort, bridgeContact, bridgeControlPort, bridgeBandwidth, webtunnelDomain, webtunnelUrl, webtunnelPort);

        createTorrcFile(torrcFilePath, config);

        model.addAttribute("successMessage", "Tor Relay configured successfully!");

        handleWebtunnelSetup(webtunnelUrl, config);
    }

    private BridgeRelayConfig createBridgeConfig(Integer bridgeTransportListenAddr, String bridgeType, String bridgeNickname, Integer bridgePort, String bridgeContact, int bridgeControlPort, Integer bridgeBandwidth, String webtunnelDomain, String webtunnelUrl, Integer webtunnelPort) {
        BridgeRelayConfig config = new BridgeRelayConfig();
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

    private void createTorrcFile(Path torrcFilePath, BridgeRelayConfig config) {
        if (!torrcFilePath.toFile().exists()) {
            TorrcFileCreator.createTorrcFile(torrcFilePath.toString(), config);
        }
    }

    private void handleWebtunnelSetup(String webtunnelUrl, BridgeRelayConfig config) throws Exception {
        if (webtunnelUrl != null && !webtunnelUrl.isEmpty()) {
            nginxService.generateNginxConfig();
            nginxService.changeRootDirectory(System.getProperty("user.dir") + "/onion/www/service-80");
            setupWebtunnel(webtunnelUrl);
            String randomString = UUID.randomUUID().toString().replace("-", "").substring(0, 24);
            nginxService.modifyNginxDefaultConfig(System.getProperty("user.dir"), randomString, webtunnelUrl);
            config.setPath(randomString); // Set the path
            updateTorrcFile(config); // Update the torrc file

            nginxService.reloadNginx();
        }
    }

    private void setupWebtunnel(String webTunnelUrl) throws Exception {

        if (!nginxService.isNginxRunning()) {
            nginxService.startNginx();
        }

        String programLocation = System.getProperty("user.dir");

        // Change the ownership of the directory
        String chownCommand = "sudo chown -R matys:matys " + programLocation + "/onion/www/service-80";
        Process chownProcess = CommandExecutor.executeCommand(chownCommand);
        if (chownProcess == null || chownProcess.exitValue() != 0) {
            throw new Exception("Failed to change ownership of the directory");
        }

        // Create the directory for the certificate files
        String certDirectory = programLocation + "/onion/certs/service-80/";
        new File(certDirectory).mkdirs();

        String command = "/home/matys/.acme.sh/acme.sh --issue -d " + webTunnelUrl + " -w " + programLocation + "/onion/www/service-80/ --nginx --server letsencrypt_test --force";
        System.out.println("Generating certificate: " + command);

        Process certProcess = CommandExecutor.executeCommand(command);
        if (certProcess == null || certProcess.exitValue() != 0) {
            throw new Exception("Failed to generate certificate");
        }
    }

    public void updateTorrcFile(BridgeRelayConfig config) throws IOException {
        String torrcFileName = TORRC_FILE_PREFIX + config.getNickname() + "_bridge";
        Path torrcFilePath = Paths.get(TORRC_DIRECTORY_PATH, torrcFileName).toAbsolutePath().normalize();

        List<String> lines = Files.readAllLines(torrcFilePath);
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("ServerTransportOptions webtunnel url")) {
                lines.set(i, "ServerTransportOptions webtunnel url=https://" + config.getWebtunnelUrl() + "/" + config.getPath());
                break;
            }
        }
        Files.write(torrcFilePath, lines);
    }

    public void configureBridge(String bridgeType, Integer bridgePort, Integer bridgeTransportListenAddr, String bridgeContact, String bridgeNickname, String webtunnelDomain, int bridgeControlPort, String webtunnelUrl, Integer webtunnelPort, Integer bridgeBandwidth, Model model) throws Exception {
        if (relayService.getBridgeCount() >= 2) {
            model.addAttribute("errorMessage", "You can only configure up to 2 bridges.");
            throw new Exception("Bridge limit reached");
        }
        configureBridgeInternal(bridgeType, bridgePort, bridgeTransportListenAddr, bridgeContact, bridgeNickname, webtunnelDomain, bridgeControlPort, webtunnelUrl, webtunnelPort, bridgeBandwidth, model);
    }
}
