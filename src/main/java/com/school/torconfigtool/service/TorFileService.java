package com.school.torconfigtool.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.school.torconfigtool.Constants.TORRC_FILE_PREFIX;

/**
 * Service class for handling Tor configuration files.
 */
@Service
public class TorFileService {

    private static final Logger logger = LoggerFactory.getLogger(TorFileService.class);

    /**
     * Builds the file path for a Tor configuration file.
     *
     * @param relayNickname the nickname of the relay.
     * @param relayType the type of the relay.
     * @return the file path as a Path object.
     */
    public Path buildTorrcFilePath(String relayNickname, String relayType) {
        return Paths.get(System.getProperty("user.dir"), "torrc", TORRC_FILE_PREFIX + relayNickname + "_" + relayType);
    }

    /**
     * Updates a Tor configuration file with the provided fingerprints.
     *
     * @param torrcFilePath the path to the Tor configuration file.
     * @param currentFingerprints the fingerprints to update the file with.
     * @throws IOException if an I/O error occurs.
     */
    public void updateTorrcWithFingerprints(Path torrcFilePath, List<String> currentFingerprints) throws IOException {
        // Read the existing torrc file content
        List<String> fileContent = new ArrayList<>();
        boolean myFamilyUpdated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(torrcFilePath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // If the MyFamily line is encountered, update it with the current fingerprints
                if (line.startsWith("MyFamily")) {
                    if (!currentFingerprints.isEmpty()) {
                        line = "MyFamily " + String.join(", ", currentFingerprints);
                        myFamilyUpdated = true;
                    } else {
                        // If there are no current fingerprints, remove the MyFamily line
                        continue;
                    }
                }
                fileContent.add(line);
            }
        }

        // If MyFamily line was not in the file and we have fingerprints, add it
        if (!myFamilyUpdated && !currentFingerprints.isEmpty()) {
            fileContent.add("MyFamily " + String.join(", ", currentFingerprints));
        }

        // Write the updated content back to the torrc file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(torrcFilePath.toFile()))) {
            for (String line : fileContent) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    /**
     * Builds the directory path for the data of a relay.
     *
     * @param relayNickname the nickname of the relay.
     * @return the directory path as a string.
     */
    public String buildDataDirectoryPath(String relayNickname) {
        return System.getProperty("user.dir") + File.separator + "torrc" + File.separator + "dataDirectory" + File.separator + relayNickname;
    }

    /**
     * Reads a fingerprint from a file.
     *
     * @param fingerprintFilePath the path to the fingerprint file.
     * @return the fingerprint as a string, or null if an error occurs.
     */
    public String readFingerprint(String fingerprintFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fingerprintFilePath))) {
            return reader.readLine().split(" ")[1].trim();
        } catch (IOException e) {
            logger.error("Failed to read fingerprint file: {}", fingerprintFilePath, e);
            return null;
        }
    }
}