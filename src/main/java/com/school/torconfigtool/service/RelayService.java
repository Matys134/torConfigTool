package com.school.torconfigtool.service;

import com.school.torconfigtool.RelayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RelayService {
    private static final Logger logger = LoggerFactory.getLogger(RelayService.class);

    public boolean arePortsAvailable(String relayNickname, int relayPort, int controlPort) {
        try {
            if (!RelayUtils.portsAreAvailable(relayNickname, relayPort, controlPort)) {
                return false;
            }

            // Other necessary code here...

            return true;
        } catch (Exception e) {
            logger.error("Error during Tor Relay configuration", e);
            return false;
        }
    }
}