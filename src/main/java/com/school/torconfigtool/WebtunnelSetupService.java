package com.school.torconfigtool;

import com.school.torconfigtool.bridge.config.BridgeRelayConfig;
import com.school.torconfigtool.util.CommandExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class WebtunnelSetupService {

    private final NginxService nginxService;

    @Autowired
    public WebtunnelSetupService(NginxService nginxService) {
        this.nginxService = nginxService;
    }

    public void setupWebtunnel(String webtunnelUrl, BridgeRelayConfig config) throws Exception {
        if (webtunnelUrl != null && !webtunnelUrl.isEmpty()) {
            nginxService.generateNginxConfig();
            nginxService.changeRootDirectory(System.getProperty("user.dir") + "/onion/www/service-80");
            setupWebtunnelInternal(webtunnelUrl);
            String randomString = UUID.randomUUID().toString().replace("-", "").substring(0, 24);
            nginxService.modifyNginxDefaultConfig(System.getProperty("user.dir"), randomString, webtunnelUrl);
            config.setPath(randomString); // Set the path
            updateTorrcFile(config); // Update the torrc file

            nginxService.reloadNginx();
        }
    }

    private void setupWebtunnelInternal(String webTunnelUrl) throws Exception {

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
        String torrcFileName = "torrc-" + config.getNickname() + "_bridge";
        Path torrcFilePath = Paths.get("torrc", torrcFileName).toAbsolutePath().normalize();

        List<String> lines = Files.readAllLines(torrcFilePath);
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("ServerTransportOptions webtunnel url")) {
                lines.set(i, "ServerTransportOptions webtunnel url=https://" + config.getWebtunnelUrl() + "/" + config.getPath());
                break;
            }
        }
        Files.write(torrcFilePath, lines);
    }
}