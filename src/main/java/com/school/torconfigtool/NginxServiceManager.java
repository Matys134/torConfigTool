// NginxServiceManager.java
package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class NginxServiceManager {

    private static final Logger logger = LoggerFactory.getLogger(NginxServiceManager.class);

    public void restartNginxService() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", "sudo systemctl reload nginx");
            processBuilder.start();
        } catch (IOException e) {
            logger.error("Error restarting Nginx service", e);
        }
    }
}