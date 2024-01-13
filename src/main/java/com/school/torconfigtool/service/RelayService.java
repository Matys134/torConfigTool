package com.school.torconfigtool.service;

import com.school.torconfigtool.RelayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FilenameFilter;

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

}