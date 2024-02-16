package com.school.torconfigtool;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    public String readFingerprint(String fingerprintFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fingerprintFilePath))) {
            return reader.readLine().split(" ")[1].trim();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

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
}