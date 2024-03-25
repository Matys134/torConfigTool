package com.school.torconfigtool.service;

import com.school.torconfigtool.model.BridgeConfig;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;

@Service
public class Obfs4Service {

    private final TorFileService torFileService;

    public Obfs4Service(TorFileService torFileService) {
        this.torFileService = torFileService;
    }

    public String getObfs4Link(String relayNickname, BridgeConfig bridgeConfig) {
        String dataDirectoryPath = System.getProperty("user.dir") + File.separator + "torrc" + File.separator + "dataDirectory";
        String obfs4FilePath = dataDirectoryPath + File.separator + relayNickname + "_BridgeConfig" + File.separator + "pt_state" + File.separator + "obfs4_bridgeline.txt";

        String obfs4Link = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(obfs4FilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Bridge obfs4")) {
                    obfs4Link = line;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read obfs4_bridgeline.txt file", e);
        }

        String fingerprintFilePath = dataDirectoryPath + File.separator + relayNickname + "_BridgeConfig" + File.separator + "fingerprint";
        String fingerprint = torFileService.readFingerprint(fingerprintFilePath);

        if (obfs4Link != null) {
            String[] parts = obfs4Link.split(" ");
            parts[2] = getPublicIPAddress(); // replace <IP ADDRESS> with public IP address
            parts[3] = bridgeConfig.getServerTransport(); // replace <PORT> with obfs4 port
            parts[4] = fingerprint; // replace <FINGERPRINT> with fingerprint
            obfs4Link = String.join(" ", parts);
        }

        return obfs4Link;
    }

    private String getPublicIPAddress() {
        try {
            URL url = new URL("https://checkip.amazonaws.com/");
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            return br.readLine().trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get public IP address", e);
        }
    }
}
