package com.school.torconfigtool.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

import static com.school.torconfigtool.Constants.TORRC_DIRECTORY_PATH;
import static com.school.torconfigtool.Constants.TORRC_FILE_PREFIX;

/**
 * Utility class for handling relay operations.
 */
@Service
public class RelayUtils {

    private static final Logger logger = LoggerFactory.getLogger(RelayUtils.class);

    /**
     * Checks for running relays and logs their process IDs.
     */
    public static void checkRunningRelays() {
        try {
            // Start a new process to get the list of running processes
            ProcessBuilder processBuilder = new ProcessBuilder("ps", "aux");
            Process process = processBuilder.start();

            try (InputStreamReader reader = new InputStreamReader(process.getInputStream())) {
                // Filter the list of processes to get the PIDs of running relays
                List<Integer> runningRelayPIDs = new BufferedReader(reader)
                        .lines()
                        .filter(line -> line.contains("tor -f " + TORRC_DIRECTORY_PATH))
                        .map(line -> line.split("\\s+"))
                        .filter(parts -> parts.length >= 2)
                        .map(parts -> Integer.parseInt(parts[1]))
                        .toList();

                // Log the PIDs of running relays
                runningRelayPIDs.forEach(pid -> logger.info("PID: {}", pid));
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // Log an error if the process execution failed
                logger.error("ps command exited with non-zero status: {}", exitCode);
            }
        } catch (IOException | InterruptedException e) {
            // Log any exceptions that occur
            logger.error("Error checking running relays", e);
        }
    }

    /**
     * Checks if a relay with the given nickname exists.
     *
     * @param relayNickname The nickname of the relay to check.
     * @return True if the relay exists, false otherwise.
     */
    public static boolean relayExists(String relayNickname) {
        String torrcDirectory = System.getProperty("user.dir") + File.separator + TORRC_DIRECTORY_PATH;

        // Check all files in the torrc directory
        File[] torrcFiles = new File(torrcDirectory).listFiles();
        if (torrcFiles != null) {
            for (File file : torrcFiles) {
                // If a file starts with the torrc file prefix and its name matches the relay nickname, the relay exists
                if (file.isFile() && file.getName().startsWith(TORRC_FILE_PREFIX)) {
                    String existingNickname = file.getName().substring(TORRC_FILE_PREFIX.length());
                    if (existingNickname.equals(relayNickname)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks if the given ports are available for a relay with the given nickname.
     *
     * @param relayNickname The nickname of the relay.
     * @param relayPort The port for the relay.
     * @param controlPort The control port for the relay.
     * @return True if the ports are available, false otherwise.
     */
    public static boolean portsAreAvailable(String relayNickname, int relayPort, int controlPort) {
        // Check if the ports are unique and not privileged
        if (!arePortsUnique(relayPort, controlPort)) {
            return false;
        }
        if (arePortsPrivileged(relayPort, controlPort)) {
            return false;
        }

        String currentDirectory = System.getProperty("user.dir");
        String torrcDirectory = currentDirectory + File.separator + "torrc";

        // Check all files in the torrc directory
        File[] torrcFiles = new File(torrcDirectory).listFiles();
        if (torrcFiles != null) {
            for (File file : torrcFiles) {
                // If a file starts with the torrc file prefix and its name contains an underscore, check its contents
                if (file.isFile() && (file.getName().startsWith(TORRC_FILE_PREFIX) && file.getName().contains("_"))) {
                    String[] fileParts = file.getName().substring(TORRC_FILE_PREFIX.length()).split("_");
                    String currentFileRelayNickname = fileParts[0];

                    // Skip the file if its name matches the relay nickname
                    if (currentFileRelayNickname.equals(relayNickname)) {
                        continue;
                    }
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            // If a line contains a port and it matches one of the given ports, the ports are not available
                            if ((line.contains("ORPort") || line.contains("ControlPort") || line.contains("HiddenServicePort")) &&
                                    (line.contains(String.valueOf(relayPort)) ||
                                            line.contains(String.valueOf(controlPort))
                                    )) {
                                return false;
                            }
                        }
                    } catch (IOException e) {
                        logger.error("Error reading Torrc file", e);
                    }
                }
            }
        }

        return true;
    }

    /**
     * Checks if the given port is available for a relay with the given nickname.
     *
     * @param relayNickname The nickname of the relay.
     * @param port The port to check.
     * @return True if the port is available, false otherwise.
     */
    public static boolean isPortAvailable(String relayNickname, int port) {
        String currentDirectory = System.getProperty("user.dir");
        String torrcDirectory = currentDirectory + File.separator + "torrc";

        // Check all files in the torrc directory
        File[] torrcFiles = new File(torrcDirectory).listFiles();
        if (torrcFiles != null) {
            for (File file : torrcFiles) {
                // If a file starts with the torrc file prefix and its name contains an underscore, check its contents
                if (file.isFile() && (file.getName().startsWith(TORRC_FILE_PREFIX) && file.getName().contains("_"))) {
                    String[] fileParts = file.getName().substring(TORRC_FILE_PREFIX.length()).split("_");
                    String currentFileRelayNickname = fileParts[0];

                    // Skip the file if its name matches the relay nickname
                    if (currentFileRelayNickname.equals(relayNickname)) {
                        continue;
                    }
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            // If a line contains a port and it matches the given port, the port is not available
                            if ((line.contains("ORPort") || line.contains("ControlPort") || line.contains("HiddenServicePort")) &&
                                    (line.contains(String.valueOf(port))
                                    )) {
                                return false;
                            }
                        }
                    } catch (IOException e) {
                        logger.error("Error reading Torrc file", e);
                    }
                }
            }
        }

        return true;
    }

    /**
     * Checks if the given ports are unique.
     *
     * @param relayPort The port for the relay.
     * @param controlPort The control port for the relay.
     * @return True if the ports are unique, false otherwise.
     */
    public static boolean arePortsUnique(int relayPort, int controlPort) {
        return relayPort != controlPort;
    }

    /**
     * Checks if the given ports are privileged (i.e., less than 1024).
     *
     * @param relayPort The port for the relay.
     * @param controlPort The control port for the relay.
     * @return True if the ports are privileged, false otherwise.
     */
    public static boolean arePortsPrivileged(int relayPort, int controlPort) {
        return relayPort < 1024 || controlPort < 1024;
    }

    public boolean arePortsAvailable(String relayNickname, int relayPort, int controlPort) {
        try {
            return RelayUtils.portsAreAvailable(relayNickname, relayPort, controlPort);
        } catch (Exception e) {
            logger.error("Error during Tor Relay configuration", e);
            return false;
        }
    }
}