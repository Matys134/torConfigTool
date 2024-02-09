package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.school.torconfigtool.TorConfigToolApplication.isProgramInstalled;

public class RelayUtils {

    private static final Logger logger = LoggerFactory.getLogger(RelayUtils.class);
    private static final String TORRC_DIRECTORY_PATH = "torrc/";
    private static final String TORRC_FILE_PREFIX = "torrc-";

    public static List<String> determineAvailableRelayTypes() {
        List<String> availableRelayTypes = new ArrayList<>();

        if (isProgramInstalled("nginx")) {
            availableRelayTypes.add("onion");
        }
        if (isProgramInstalled("obfs4proxy")) {
            availableRelayTypes.add("bridge");
        }
        return availableRelayTypes;
    }

    public static void checkRunningRelays() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("ps", "aux");
            Process process = processBuilder.start();

            try (InputStreamReader reader = new InputStreamReader(process.getInputStream())) {
                List<Integer> runningRelayPIDs = new BufferedReader(reader)
                        .lines()
                        .filter(line -> line.contains("tor -f torrc-"))
                        .map(line -> line.split("\\s+"))
                        .filter(parts -> parts.length >= 2)
                        .map(parts -> Integer.parseInt(parts[1]))
                        .toList();

                runningRelayPIDs.forEach(pid -> logger.info("PID: {}", pid));
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // Handle process execution failure
                logger.error("ps command exited with non-zero status: {}", exitCode);
            }
        } catch (IOException | InterruptedException e) {
            // Handle exceptions
            logger.error("Error checking running relays", e);
        }
    }

    public static boolean relayExists(String relayNickname) {
        String torrcDirectory = System.getProperty("user.dir") + File.separator + TORRC_DIRECTORY_PATH;

        File[] torrcFiles = new File(torrcDirectory).listFiles();
        if (torrcFiles != null) {
            for (File file : torrcFiles) {
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

    // Check if the ports are available by checking torrc files and running processes
    public static boolean portsAreAvailable(String relayNickname, int relayPort, int controlPort) {
        if (!arePortsUnique(relayPort, controlPort)) {
            return false;
        }
        if (arePortsPrivileged(relayPort, controlPort)) {
            return false;
        }

        String currentDirectory = System.getProperty("user.dir");
        String torrcDirectory = currentDirectory + File.separator + "torrc";

        // Check torrc files
        File[] torrcFiles = new File(torrcDirectory).listFiles();
        if (torrcFiles != null) {
            for (File file : torrcFiles) {
                if (file.isFile() && (file.getName().startsWith("torrc-") && file.getName().contains("_"))) {
                    String[] fileParts = file.getName().substring("torrc-".length()).split("_");
                    String currentFileRelayNickname = fileParts[0];

                    if (currentFileRelayNickname.equals(relayNickname)) {
                        continue;
                    }
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
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

    public static boolean isPortAvailable(String relayNickname, int port) {
        String currentDirectory = System.getProperty("user.dir");
        String torrcDirectory = currentDirectory + File.separator + "torrc";

        // Check torrc files
        File[] torrcFiles = new File(torrcDirectory).listFiles();
        if (torrcFiles != null) {
            for (File file : torrcFiles) {
                if (file.isFile() && (file.getName().startsWith("torrc-") && file.getName().contains("_"))) {
                    String[] fileParts = file.getName().substring("torrc-".length()).split("_");
                    String currentFileRelayNickname = fileParts[0];

                    if (currentFileRelayNickname.equals(relayNickname)) {
                        continue;
                    }
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
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


    public static boolean arePortsUnique(int relayPort, int controlPort) {
        return relayPort != controlPort;
    }

    public static boolean arePortsPrivileged(int relayPort, int controlPort) {
        return relayPort < 1024 || controlPort < 1024;
    }
}
