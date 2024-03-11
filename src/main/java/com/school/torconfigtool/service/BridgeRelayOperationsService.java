package com.school.torconfigtool.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static com.school.torconfigtool.util.Constants.TORRC_FILE_PREFIX;

@Service
public class BridgeRelayOperationsService {
    private final TorFileService torFileService;

    public BridgeRelayOperationsService(TorFileService torFileService) {
        this.torFileService = torFileService;
    }

    public String getWebtunnelLink(String relayNickname) {
        String dataDirectoryPath = System.getProperty("user.dir") + File.separator + "torrc" + File.separator + "dataDirectory";
        String fingerprintFilePath = dataDirectoryPath + File.separator + relayNickname + "_BridgeConfig" + File.separator + "fingerprint";
        String fingerprint = torFileService.readFingerprint(fingerprintFilePath);

        String torrcFilePath = System.getProperty("user.dir") + File.separator + "torrc" + File.separator + TORRC_FILE_PREFIX + relayNickname + "_bridge";

        String webtunnelDomainAndPath = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(torrcFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("ServerTransportOptions webtunnel url")) {
                    webtunnelDomainAndPath = line.split("=")[1].trim();
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read torrc file", e);
        }

        return "webtunnel 10.0.0.2:443 " + fingerprint + " url=" + webtunnelDomainAndPath;
    }
}