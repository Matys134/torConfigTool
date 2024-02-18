package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Service
public class BridgeRelayOperationsService {

    private static final Logger logger = LoggerFactory.getLogger(BridgeRelayOperationsService.class);
    private final TorFileService torFileService;

    public BridgeRelayOperationsService(TorFileService torFileService) {
        this.torFileService = torFileService;
    }

    public String getWebtunnelLink(String relayNickname) {
        String dataDirectoryPath = torFileService.buildDataDirectoryPath(relayNickname);
        String fingerprintFilePath = dataDirectoryPath + File.separator + "fingerprint";
        String fingerprint = torFileService.readFingerprint(fingerprintFilePath);

        // Construct the path to the torrc file
        String torrcFilePath = System.getProperty("user.dir") + File.separator + "torrc" + File.separator + "torrc-" + relayNickname + "_bridge";

        String webtunnelDomainAndPath = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(torrcFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Check if the line starts with "ServerTransportOptions webtunnel url"
                if (line.startsWith("ServerTransportOptions webtunnel url")) {
                    // Extract the webtunnel domain and path from the line
                    webtunnelDomainAndPath = line.split("=")[1].trim();
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("Failed to read torrc file: {}", torrcFilePath, e);
        }

        // Replace the "https://yourdomain/path" in the webtunnel link with the extracted webtunnel domain and path

        return "webtunnel 10.0.0.2:443 " + fingerprint + " url=" + webtunnelDomainAndPath;
    }
}
