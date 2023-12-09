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
        if(!arePortsUnique(relayPort, controlPort)){
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
                            if ((line.contains("ORPort") || line.contains("ControlPort")) &&
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

        // Check running processes
        try {
            ProcessBuilder psProcessBuilder = new ProcessBuilder("ps", "aux");
            Process psProcess = psProcessBuilder.start();

            ProcessBuilder netstatProcessBuilder = new ProcessBuilder("netstat", "-tulpn");
            Process netstatProcess = netstatProcessBuilder.start();

            // Wait for the processes to complete
            int psExitCode = psProcess.waitFor();
            int netstatExitCode = netstatProcess.waitFor();

            if (psExitCode != 0 || netstatExitCode != 0) {
                logger.error("ps or netstat command exited with non-zero status: ps={}, netstat={}", psExitCode, netstatExitCode);
                return false; // Consider the ports unavailable in case of a command failure
            }

            // Read the output of ps command
            List<Integer> runningRelayPIDs;
            try (BufferedReader psReader = new BufferedReader(new InputStreamReader(psProcess.getInputStream()))) {
                runningRelayPIDs = psReader.lines()
                        .filter(line -> line.contains("tor -f torrc-"))
                        .map(line -> line.split("\\s+"))
                        .filter(parts -> parts.length >= 2)
                        .map(parts -> Integer.parseInt(parts[1]))
                        .toList();
            }

            // Check ports using netstat for each running process
            for (Integer pid : runningRelayPIDs) {
                ProcessBuilder netstatForPidProcessBuilder = new ProcessBuilder("netstat", "-tulpn");
                Process netstatForPidProcess = netstatForPidProcessBuilder.start();

                try (BufferedReader netstatReader = new BufferedReader(new InputStreamReader(netstatForPidProcess.getInputStream()))) {
                    List<String> netstatOutput = netstatReader.lines().toList();
                    for (String netstatLine : netstatOutput) {
                        if (netstatLine.contains(String.valueOf(relayPort)) || netstatLine.contains(String.valueOf(controlPort))) {
                            return false;
                        }
                    }
                }

                // Wait for the netstat process to complete
                int netstatForPidExitCode = netstatForPidProcess.waitFor();
                if (netstatForPidExitCode != 0) {
                    logger.error("netstat command for PID {} exited with non-zero status: {}", pid, netstatForPidExitCode);
                    return false; // Consider the ports unavailable in case of a command failure
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error checking port availability", e);
            return false;
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
                            if ((line.contains("ORPort") || line.contains("ControlPort")) &&
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

        // Check running processes
        try {
            ProcessBuilder psProcessBuilder = new ProcessBuilder("ps", "aux");
            Process psProcess = psProcessBuilder.start();

            ProcessBuilder netstatProcessBuilder = new ProcessBuilder("netstat", "-tulpn");
            Process netstatProcess = netstatProcessBuilder.start();

            // Wait for the processes to complete
            int psExitCode = psProcess.waitFor();
            int netstatExitCode = netstatProcess.waitFor();

            if (psExitCode != 0 || netstatExitCode != 0) {
                logger.error("ps or netstat command exited with non-zero status: ps={}, netstat={}", psExitCode, netstatExitCode);
                return false; // Consider the ports unavailable in case of a command failure
            }

            // Read the output of ps command
            List<Integer> runningRelayPIDs;
            try (BufferedReader psReader = new BufferedReader(new InputStreamReader(psProcess.getInputStream()))) {
                runningRelayPIDs = psReader.lines()
                        .filter(line -> line.contains("tor -f torrc-"))
                        .map(line -> line.split("\\s+"))
                        .filter(parts -> parts.length >= 2)
                        .map(parts -> Integer.parseInt(parts[1]))
                        .toList();
            }

            // Check ports using netstat for each running process
            for (Integer pid : runningRelayPIDs) {
                ProcessBuilder netstatForPidProcessBuilder = new ProcessBuilder("netstat", "-tulpn");
                Process netstatForPidProcess = netstatForPidProcessBuilder.start();

                try (BufferedReader netstatReader = new BufferedReader(new InputStreamReader(netstatForPidProcess.getInputStream()))) {
                    List<String> netstatOutput = netstatReader.lines().toList();
                    for (String netstatLine : netstatOutput) {
                        if (netstatLine.contains(String.valueOf(port))) {
                            return false;
                        }
                    }
                }

                // Wait for the netstat process to complete
                int netstatForPidExitCode = netstatForPidProcess.waitFor();
                if (netstatForPidExitCode != 0) {
                    logger.error("netstat command for PID {} exited with non-zero status: {}", pid, netstatForPidExitCode);
                    return false; // Consider the ports unavailable in case of a command failure
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error checking port availability", e);
            return false;
        }

        return true;
    }



    public static boolean arePortsUnique(int relayPort, int controlPort){
        return relayPort != controlPort;
    }
}
