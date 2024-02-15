package com.school.torconfigtool.nginx.service;

import com.school.torconfigtool.NginxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This class is responsible for managing the Nginx service.
 * It uses the NginxService to perform operations on the Nginx service.
 */
@Service
public class NginxServiceManager {

    private static final Logger logger = LoggerFactory.getLogger(NginxServiceManager.class);
    private final NginxService nginxService;

    /**
     * Constructor for the NginxServiceManager class.
     * @param nginxService The NginxService to be used for managing the Nginx service.
     */
    public NginxServiceManager(NginxService nginxService) {
        this.nginxService = nginxService;
    }

    /**
     * This method is used to restart the Nginx service.
     * It uses the reloadNginx method of the NginxService.
     * If an exception occurs during the restart, it is caught and logged.
     */
    public void restartNginxService() {
        try {
            nginxService.reloadNginx();
        } catch (Exception e) {
            logger.error("Unexpected error restarting Nginx service", e);
        }
    }
}