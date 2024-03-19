package com.school.torconfigtool.service;

import com.school.torconfigtool.model.BridgeConfig;
import com.simtechdata.waifupnp.UPnP;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.school.torconfigtool.util.Constants.TORRC_FILE_PREFIX;

/**
 * Service class for managing web tunnels.
 */
@Service
public class WebtunnelService {
    private final NginxService nginxService;
    private final AcmeService acmeService;
    private final CommandService commandService;
    private final TorFileService torFileService;
    private static final String TORRC_DIRECTORY_PATH = "torrc";

    /**
     * Constructor for WebtunnelService.
     *
     * @param nginxService The NginxService to use.
     */
    public WebtunnelService(NginxService nginxService, AcmeService acmeService, CommandService commandService, TorFileService torFileService) {
        this.nginxService = nginxService;
        this.acmeService = acmeService;
        this.commandService = commandService;
        this.torFileService = torFileService;
    }

    /**
     * Sets up a web tunnel.
     *
     * @param webTunnelUrl The URL of the web tunnel.
     * @throws Exception If an error occurs during setup.
     */
    public void setupWebtunnel(String webTunnelUrl, int webtunnelPort) throws Exception {
        UPnP.openPortTCP(80);

        // Check if Nginx is running, if not, start it
        if (!nginxService.isNginxRunning()) {
            nginxService.startNginx();
        }

        // Get the current program location
        String programLocation = System.getProperty("user.dir");

        String username = System.getProperty("user.name");
        String chownCommand = "sudo chown -R " + username + ":" + username + " " + programLocation + "/onion/www/service-" + webtunnelPort;
        Process chownProcess = commandService.executeCommand(chownCommand);
        if (chownProcess == null || chownProcess.exitValue() != 0) {
            throw new Exception("Failed to change ownership of the directory");
        }

        // Call the new method to generate the certificate
        acmeService.generateCertificate(webTunnelUrl, programLocation, webtunnelPort);

        UPnP.closePortTCP(80);
    }

    /**
     * Updates the torrc file with the given BridgeConfig.
     *
     * @param config The BridgeConfig to use.
     * @throws IOException If an error occurs during file operations.
     */
    public void updateTorrcFile(BridgeConfig config) throws IOException {
        String torrcFileName = TORRC_FILE_PREFIX + config.getNickname() + "_bridge";
        Path torrcFilePath = Paths.get(TORRC_DIRECTORY_PATH, torrcFileName).toAbsolutePath().normalize();

        // Read all lines from the file
        List<String> lines = Files.readAllLines(torrcFilePath);
        for (int i = 0; i < lines.size(); i++) {
            // Update the line that starts with "ServerTransportOptions webtunnel url"
            if (lines.get(i).startsWith("ServerTransportOptions webtunnel url")) {
                lines.set(i, "ServerTransportOptions webtunnel url=https://" + config.getWebtunnelUrl() + "/" + config.getPath());
                break;
            }
        }
        // Write the updated lines back to the file
        Files.write(torrcFilePath, lines);
    }

    public String getWebtunnelLink(String relayNickname) {
        String dataDirectoryPath = System.getProperty("user.dir") + File.separator + "torrc" + File.separator + "dataDirectory";
        String fingerprintFilePath = dataDirectoryPath + File.separator + relayNickname + "_BridgeConfig" + File.separator + "fingerprint";
        String fingerprint = torFileService.readFingerprint(fingerprintFilePath);

        String torrcFilePath = System.getProperty("user.dir") + File.separator + "torrc" + File.separator + TORRC_FILE_PREFIX + relayNickname + "_bridge";

        String webtunnelDomainAndPath = null;
        int webtunnelPort = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(torrcFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("ServerTransportOptions webtunnel url")) {
                    webtunnelDomainAndPath = line.split("=")[1].trim();
                } else if (line.startsWith("# webtunnel")) {
                    webtunnelPort = Integer.parseInt(line.split(" ")[1].trim());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read torrc file", e);
        }

        return "webtunnel 10.0.0.2:" + webtunnelPort + " " + fingerprint + " url=" + webtunnelDomainAndPath;
    }
}