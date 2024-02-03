package com.school.torconfigtool.service;

import com.school.torconfigtool.RelayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Scanner;

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

}