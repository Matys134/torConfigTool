package com.school.torconfigtool.service;

import com.school.torconfigtool.model.BridgeConfig;
import com.school.torconfigtool.model.GuardConfig;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static com.school.torconfigtool.util.Constants.TORRC_DIRECTORY_PATH;
import static com.school.torconfigtool.util.Constants.TORRC_FILE_PREFIX;

/**
 * Service class for managing relay information.
 */
@Service
public class RelayInformationService {

    /**
     * Returns the count of bridge files in the TORRC directory.
     *
     * @return the count of bridge files.
     * @throws RuntimeException if the TORRC directory does not exist or is not a directory.
     */
    public int getBridgeCount() {
        File torrcDirectory = new File(TORRC_DIRECTORY_PATH);
        if (!torrcDirectory.exists() || !torrcDirectory.isDirectory()) {
            throw new RuntimeException("Directory " + TORRC_DIRECTORY_PATH + " does not exist or is not a directory.");
        }

        String[] files = torrcDirectory.list(new FilenameFilter() {
            private static final String TORRC_FILE_SUFFIX = "_bridge";

            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(TORRC_FILE_PREFIX) && name.endsWith(TORRC_FILE_SUFFIX);
            }
        });

        return files != null ? files.length : 0;
    }

    /**
     * Returns the count of guard files in the TORRC directory.
     *
     * @return the count of guard files.
     * @throws RuntimeException if the TORRC directory does not exist or is not a directory.
     */
    public int getGuardCount() {
        File torrcDirectory = new File(TORRC_DIRECTORY_PATH);
        if (!torrcDirectory.exists() || !torrcDirectory.isDirectory()) {
            throw new RuntimeException("Directory " + TORRC_DIRECTORY_PATH + " does not exist or is not a directory.");
        }

        String[] files = torrcDirectory.list(new FilenameFilter() {
            private static final String TORRC_FILE_SUFFIX = "_guard";

            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(TORRC_FILE_PREFIX) && name.endsWith(TORRC_FILE_SUFFIX);
            }
        });

        return files != null ? files.length : 0;
    }

    /**
     * Returns a map of running bridge types with their nicknames.
     *
     * @return a map of running bridge types with their nicknames.
     * @throws RuntimeException if there is an error reading the torrc file.
     */
    public Map<String, String> getConfiguredBridgeType() {
        File torrcDirectory = new File(TORRC_DIRECTORY_PATH);
        File[] files = torrcDirectory.listFiles((dir, name) -> name.startsWith(TORRC_FILE_PREFIX) && name.endsWith("_bridge"));
        Map<String, String> runningBridgeTypes = new HashMap<>();

        if (files != null) {
            for (File file : files) {
                try {
                    String content = new String(Files.readAllBytes(file.toPath()));
                    String bridgeType = null;
                    if (content.contains("obfs4")) {
                        bridgeType = "obfs4";
                    } else if (content.contains("webtunnel")) {
                        bridgeType = "webtunnel";
                    } else if (content.contains("snowflake")) {
                        bridgeType = "snowflake";
                    }
                    if (bridgeType != null) {
                        String bridgeNickname = file.getName().substring(TORRC_FILE_PREFIX.length(), file.getName().length() - "_bridge".length());
                        runningBridgeTypes.put(bridgeNickname, bridgeType);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Error reading torrc file", e);
                }
            }
        }

        if (new File(torrcDirectory, "snowflake_proxy_running").exists()) {
            runningBridgeTypes.put("snowflake_proxy", "snowflake");
        }

        return runningBridgeTypes;
    }

    /**
     * Returns a map of bridge types with their counts.
     *
     * @return a map of bridge types with their counts.
     */
    public Map<String, Integer> getBridgeCountByType() {
        Map<String, Integer> bridgeCountByType = new HashMap<>();
        bridgeCountByType.put("obfs4", 0);
        bridgeCountByType.put("webtunnel", 0);
        bridgeCountByType.put("snowflake", 0);

        // Get the list of all bridges
        List<BridgeConfig> bridges = getAllBridges();

        // Count the number of each type of bridge
        for (BridgeConfig bridge : bridges) {
            String bridgeType = bridge.getBridgeType();
            bridgeCountByType.put(bridgeType, bridgeCountByType.get(bridgeType) + 1);
        }

        // Count the number of running snowflake proxies
        if (new File(TORRC_DIRECTORY_PATH, "snowflake_proxy_running").exists()) {
            bridgeCountByType.put("snowflake", bridgeCountByType.get("snowflake") + 1);
        }

        return bridgeCountByType;
    }

    /**
     * Returns a list of all bridges.
     *
     * @return a list of all bridges.
     * @throws RuntimeException if there is an error reading the torrc file.
     */
    public List<BridgeConfig> getAllBridges() {
        List<BridgeConfig> bridges = new ArrayList<>();
        File torrcDirectory = new File(TORRC_DIRECTORY_PATH);
        File[] files = torrcDirectory.listFiles((dir, name) -> name.startsWith(TORRC_FILE_PREFIX) && name.endsWith("_bridge"));

        if (files != null) {
            for (File file : files) {
                try (Scanner scanner = new Scanner(file)) {
                    BridgeConfig bridge = new BridgeConfig();
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        if (line.startsWith("Nickname")) {
                            bridge.setNickname(line.split(" ")[1]);
                        } else if (line.startsWith("ORPort")) {
                            bridge.setOrPort(line.split(" ")[1]);
                        } else if (line.startsWith("Contact")) {
                            bridge.setContact(line.split(" ")[1]);
                        } else if (line.startsWith("ControlPort")) {
                            bridge.setControlPort(line.split(" ")[1]);
                        } else if (line.startsWith("ServerTransportPlugin")) {
                            bridge.setBridgeType(line.split(" ")[1]);
                        }
                    }
                    bridges.add(bridge);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("Error reading torrc file", e);
                }
            }
        }

        return bridges;
    }

    /**
     * Returns a list of all guards.
     *
     * @return a list of all guards.
     * @throws RuntimeException if there is an error reading the torrc file.
     */
    public List<GuardConfig> getAllGuards() {
        List<GuardConfig> guards = new ArrayList<>();
        File torrcDirectory = new File(TORRC_DIRECTORY_PATH);
        File[] files = torrcDirectory.listFiles((dir, name) -> name.startsWith(TORRC_FILE_PREFIX) && name.endsWith("_guard"));

        if (files != null) {
            for (File file : files) {
                try (Scanner scanner = new Scanner(file)) {
                    GuardConfig guard = new GuardConfig();
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        if (line.startsWith("Nickname")) {
                            guard.setNickname(line.split(" ")[1]);
                        } else if (line.startsWith("ORPort")) {
                            guard.setOrPort(line.split(" ")[1]);
                        } else if (line.startsWith("Contact")) {
                            guard.setContact(line.split(" ")[1]);
                        } else if (line.startsWith("ControlPort")) {
                            guard.setControlPort(line.split(" ")[1]);
                        }
                    }
                    guards.add(guard);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("Error reading torrc file", e);
                }
            }
        }

        return guards;
    }

    private static boolean isLimitOn = true;

    public static void toggleLimit() {
        isLimitOn = !isLimitOn;
    }

    public static boolean isLimitOn() {
        return isLimitOn;
    }

}