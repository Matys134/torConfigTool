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

    /**
     * Checks if a relay with the given nickname exists.
     *
     * @param relayNickname the nickname of the relay
     * @return true if the relay exists, false otherwise
     */
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

    /**
     * Checks if the given ports are available for the relay with the given nickname.
     *
     * @param relayNickname the nickname of the relay
     * @param ports the ports to check
     * @return true if the ports are available, false otherwise
     */
    public static boolean portsAreAvailable(String relayNickname, int... ports) {
        String currentDirectory = System.getProperty("user.dir");
        String torrcDirectory = currentDirectory + File.separator + "torrc";

        // Add reserved ports
        int[] reservedPorts = {80, 443};
        int[] allPorts = new int[ports.length + reservedPorts.length];
        System.arraycopy(ports, 0, allPorts, 0, ports.length);
        System.arraycopy(reservedPorts, 0, allPorts, ports.length, reservedPorts.length);

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
                            for (int port : allPorts) {
                                if ((line.contains("ORPort") || line.contains("ControlPort") || line.contains("HiddenServicePort") || line.contains("ServerTransportListenAddr")) &&
                                        (line.contains(String.valueOf(port)))) {
                                    return false;
                                }
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to read torrc file: " + e.getMessage());
                    }
                }
            }
        }
        return true;
    }
}