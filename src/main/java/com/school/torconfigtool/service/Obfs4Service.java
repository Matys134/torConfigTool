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

        File obfs4File = new File(obfs4FilePath);
        if (!obfs4File.exists()) {
            throw new RuntimeException("obfs4_bridgeline.txt file does not exist");
        }

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
            parts[2] = getPublicIPAddress() + ":" + bridgeConfig.getServerTransport(); // combine <IP ADDRESS> and <PORT> with :
            parts[4] = fingerprint; // replace <FINGERPRINT> with fingerprint

            // Remove the element at index 3
            String[] newParts = new String[parts.length - 1];
            System.arraycopy(parts, 0, newParts, 0, 3);
            System.arraycopy(parts, 4, newParts, 3, parts.length - 4);

            obfs4Link = String.join(" ", newParts);
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
