package com.school.torconfigtool.service;

import com.school.torconfigtool.model.BridgeConfig;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;

/**
 * Obfs4Service is a service class that handles operations related to Obfs4.
 */
@Service
public class Obfs4Service {

    private final TorFileService torFileService;

    /**
     * Constructor for Obfs4Service.
     * @param torFileService The service to handle Tor file operations.
     */
    public Obfs4Service(TorFileService torFileService) {
        this.torFileService = torFileService;
    }

    /**
     * Gets the Obfs4 link for a given relay and bridge configuration.
     * @param relayNickname The nickname of the relay.
     * @param bridgeConfig The bridge configuration.
     * @return The Obfs4 link, or null if the Obfs4 file does not exist.
     */
    public String getObfs4Link(String relayNickname, BridgeConfig bridgeConfig) {
        String dataDirectoryPath = System.getProperty("user.dir") + File.separator + "torrc" + File.separator + "dataDirectory";
        String obfs4FilePath = dataDirectoryPath + File.separator + relayNickname + "_BridgeConfig" + File.separator + "pt_state" + File.separator + "obfs4_bridgeline.txt";

        File obfs4File = new File(obfs4FilePath);
        if (!obfs4File.exists()) {
            return null;
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

    /**
     * Gets the public IP address of the current machine.
     * @return The public IP address.
     * @throws RuntimeException If there is an error while getting the public IP address.
     */
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