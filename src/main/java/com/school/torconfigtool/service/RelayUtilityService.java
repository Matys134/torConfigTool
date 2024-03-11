package com.school.torconfigtool.service;

import org.springframework.stereotype.Service;

import java.io.*;

import static com.school.torconfigtool.util.Constants.TORRC_DIRECTORY_PATH;
import static com.school.torconfigtool.util.Constants.TORRC_FILE_PREFIX;

/**
 * Utility class for handling relay operations.
 */
@Service
public class RelayUtilityService {

    public static boolean relayExists(String relayNickname) {
        String torrcDirectory = System.getProperty("user.dir") + File.separator + TORRC_DIRECTORY_PATH;

        File[] torrcFiles = new File(torrcDirectory).listFiles();
        if (torrcFiles != null) {
            for (File file : torrcFiles) {
                if (file.isFile() && file.getName().startsWith(TORRC_FILE_PREFIX)) {
                    String[] parts = file.getName().substring(TORRC_FILE_PREFIX.length()).split("_");
                    String existingNickname = parts[0];
                    if (existingNickname.equals(relayNickname)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean portsAreAvailable(String relayNickname, int... ports) {
        String currentDirectory = System.getProperty("user.dir");
        String torrcDirectory = currentDirectory + File.separator + "torrc";

        File[] torrcFiles = new File(torrcDirectory).listFiles();
        if (torrcFiles != null) {
            for (File file : torrcFiles) {
                if (file.isFile() && (file.getName().startsWith(TORRC_FILE_PREFIX) && file.getName().contains("_"))) {
                    String[] fileParts = file.getName().substring(TORRC_FILE_PREFIX.length()).split("_");
                    String currentFileRelayNickname = fileParts[0];

                    if (currentFileRelayNickname.equals(relayNickname)) {
                        continue;
                    }
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            for (int port : ports) {
                                if ((line.contains("ORPort") || line.contains("ControlPort") || line.contains("HiddenServicePort")) &&
                                        (line.contains(String.valueOf(port)))) {
                                    return false;
                                }
                            }
                        }
                    } catch (IOException e) {
                        // Handle exceptions as needed
                    }
                }
            }
        }

        return true;
    }

    public boolean arePortsAvailable(String relayNickname, int relayPort, int controlPort) {
        try {
            return RelayUtilityService.portsAreAvailable(relayNickname, relayPort, controlPort);
        } catch (Exception e) {
            // Handle exceptions as needed
            return false;
        }
    }
}