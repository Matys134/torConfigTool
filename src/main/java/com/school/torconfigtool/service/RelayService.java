package com.school.torconfigtool.service;

import com.school.torconfigtool.util.RelayUtils;
import com.school.torconfigtool.model.BridgeConfig;
import com.school.torconfigtool.model.GuardConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
public class RelayService {
    private static final Logger logger = LoggerFactory.getLogger(RelayService.class);
    private static final String TORRC_DIRECTORY_PATH = "torrc/";
    private static final String TORRC_FILE_PREFIX = "torrc-";

    public boolean arePortsAvailable(String relayNickname, int relayPort, int controlPort) {
        try {
            return RelayUtils.portsAreAvailable(relayNickname, relayPort, controlPort);
        } catch (Exception e) {
            logger.error("Error during Tor Relay configuration", e);
            return false;
        }
    }

    public int getBridgeCount() {
        File torrcDirectory = new File(TORRC_DIRECTORY_PATH);
        if (!torrcDirectory.exists() || !torrcDirectory.isDirectory()) {
            logger.error("Directory " + TORRC_DIRECTORY_PATH + " does not exist or is not a directory.");
            return 0;
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

    public int getGuardCount() {
        File torrcDirectory = new File(TORRC_DIRECTORY_PATH);
        if (!torrcDirectory.exists() || !torrcDirectory.isDirectory()) {
            logger.error("Directory " + TORRC_DIRECTORY_PATH + " does not exist or is not a directory.");
            return 0;
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

    public Map<String, String> getRunningBridgeType() {
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
                    logger.error("Error reading torrc file", e);
                }
            }
        }

        if (new File(torrcDirectory, "snowflake_proxy_running").exists()) {
            runningBridgeTypes.put("snowflake_proxy", "snowflake");
        }

        return runningBridgeTypes;
    }

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
                    logger.error("Error reading torrc file", e);
                }
            }
        }

        return bridges;
    }

    // method to get all guard relays
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
                    logger.error("Error reading torrc file", e);
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