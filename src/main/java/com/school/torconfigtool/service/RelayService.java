package com.school.torconfigtool.service;

import com.school.torconfigtool.RelayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RelayService {
    private static final Logger logger = LoggerFactory.getLogger(RelayService.class);

    public boolean arePortsAvailable(String relayNickname, int relayPort, int controlPort, int socksPort) {
        try {
            if (!RelayUtils.portsAreAvailable(relayNickname, relayPort, controlPort, socksPort)) {
                return false;
            }

            // Other necessary code here...

            return true;
        } catch (Exception e) {
            logger.error("Error during Tor Relay configuration", e);
            return false;
        }
    }
    public boolean isPortAvailable(String relayNickname, int relayPort) {
        try {
            // Assuming the controlPort and socksPort is same as relayPort for the bridge.
            // Update based on your actual requirements.
            if (!RelayUtils.portsAreAvailable(relayNickname, relayPort, relayPort, relayPort)) {
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