package com.school.torconfigtool.service;

import com.school.torconfigtool.RelayUtils;
import com.school.torconfigtool.models.BridgeRelayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.*;

@Service
public class RelayService {
    private static final Logger logger = LoggerFactory.getLogger(RelayService.class);
    private static final String TORRC_DIRECTORY_PATH = "torrc/";
    private static final String TORRC_FILE_PREFIX = "torrc-";

    public boolean arePortsAvailable(String relayNickname, int relayPort, int controlPort) {
        try {
            return RelayUtils.portsAreAvailable(relayNickname, relayPort, controlPort);

            // Other necessary code here...
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

    public String getRunningBridgeType() {
        File torrcDirectory = new File(TORRC_DIRECTORY_PATH);
        File[] files = torrcDirectory.listFiles((dir, name) -> name.startsWith(TORRC_FILE_PREFIX) && name.endsWith("_bridge"));
        String runningBridgeType = null;

        if (files != null) {
            for (File file : files) {
                try (Scanner scanner = new Scanner(file)) {
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        if (line.startsWith("ServerTransportPlugin")) {
                            runningBridgeType = line.split(" ")[1];
                            break;
                        }
                    }
                } catch (FileNotFoundException e) {
                    logger.error("Error reading torrc file", e);
                }
            }
        }

        if (new File(torrcDirectory, "snowflake_proxy_running").exists()) {
            runningBridgeType = "snowflake";
        }

        return runningBridgeType;
    }

    public Map<String, Integer> getBridgeCountByType() {
        Map<String, Integer> bridgeCountByType = new HashMap<>();
        bridgeCountByType.put("obfs4", 0);
        bridgeCountByType.put("webtunnel", 0);
        bridgeCountByType.put("snowflake", 0);

        File torrcDirectory = new File(TORRC_DIRECTORY_PATH);
        if (!torrcDirectory.exists() || !torrcDirectory.isDirectory()) {
            logger.error("Directory " + TORRC_DIRECTORY_PATH + " does not exist or is not a directory.");
            return bridgeCountByType;
        }

        File[] files = torrcDirectory.listFiles((dir, name) -> name.startsWith(TORRC_FILE_PREFIX) && name.contains("_obfs4"));
        bridgeCountByType.put("obfs4", files != null ? files.length : 0);

        files = torrcDirectory.listFiles((dir, name) -> name.startsWith(TORRC_FILE_PREFIX) && name.contains("_webtunnel"));
        bridgeCountByType.put("webtunnel", files != null ? files.length : 0);

        files = torrcDirectory.listFiles((dir, name) -> name.startsWith(TORRC_FILE_PREFIX) && name.contains("_snowflake"));
        bridgeCountByType.put("snowflake", files != null ? files.length : 0);

        return bridgeCountByType;
    }

    public List<BridgeRelayConfig> getAllBridges() {
        List<BridgeRelayConfig> bridges = new ArrayList<>();
        File torrcDirectory = new File(TORRC_DIRECTORY_PATH);
        File[] files = torrcDirectory.listFiles((dir, name) -> name.startsWith(TORRC_FILE_PREFIX) && name.endsWith("_bridge"));

        if (files != null) {
            for (File file : files) {
                try (Scanner scanner = new Scanner(file)) {
                    BridgeRelayConfig bridge = new BridgeRelayConfig();
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

    private static boolean isLimitOn = true;

    public static void toggleLimit() {
        isLimitOn = !isLimitOn;
    }

    public static boolean isLimitOn() {
        return isLimitOn;
    }

}