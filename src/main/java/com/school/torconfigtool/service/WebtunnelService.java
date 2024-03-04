package com.school.torconfigtool.service;

import com.school.torconfigtool.model.BridgeConfig;
import com.simtechdata.waifupnp.UPnP;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.school.torconfigtool.Constants.TORRC_FILE_PREFIX;

/**
 * Service class for managing web tunnels.
 */
@Service
public class WebtunnelService {
    private final NginxService nginxService;
    private final AcmeService acmeService;
    private final CommandService commandService;
    private static final String TORRC_DIRECTORY_PATH = "torrc";

    /**
     * Constructor for WebtunnelService.
     *
     * @param nginxService The NginxService to use.
     */
    public WebtunnelService(NginxService nginxService, AcmeService acmeService, CommandService commandService) {
        this.nginxService = nginxService;
        this.acmeService = acmeService;
        this.commandService = commandService;
    }

    /**
     * Sets up a web tunnel.
     *
     * @param webTunnelUrl The URL of the web tunnel.
     * @throws Exception If an error occurs during setup.
     */
    public void setupWebtunnel(String webTunnelUrl) throws Exception {
        UPnP.openPortTCP(80);

        // Check if Nginx is running, if not, start it
        if (!nginxService.isNginxRunning()) {
            nginxService.startNginx();
        }

        // Get the current program location
        String programLocation = System.getProperty("user.dir");

        String username = System.getProperty("user.name");
        String chownCommand = "sudo chown -R " + username + ":" + username + " " + programLocation + "/onion/www/service-80";
        Process chownProcess = commandService.executeCommand(chownCommand);
        if (chownProcess == null || chownProcess.exitValue() != 0) {
            throw new Exception("Failed to change ownership of the directory");
        }

        // Call the new method to generate the certificate
        acmeService.generateCertificate(webTunnelUrl, programLocation);
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
}