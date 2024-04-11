package com.school.torconfigtool.service;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * NginxServiceManager is a service class that manages the status of the Nginx service.
 * It implements the StatusChangeListener interface to listen for status changes.
 */
@Service
public class NginxServiceManager implements StatusChangeListener {

    private final RelayStatusService relayStatusService;
    private final NginxService nginxService;

    /**
     * Constructor for NginxServiceManager.
     * @param relayStatusService The service to handle relay status operations.
     * @param nginxService The service to handle Nginx operations.
     */
    public NginxServiceManager(RelayStatusService relayStatusService, NginxService nginxService) {
        this.relayStatusService = relayStatusService;
        this.nginxService = nginxService;
        this.relayStatusService.setStatusChangeListener(this);
    }

    /**
     * Handles the status change event.
     * @param status The new status.
     */
    @Override
    public void onStatusChange(String status) {
        checkAndManageNginxStatus();
    }

    /**
     * Checks and manages the status of the Nginx service.
     * If at least one Onion or Web Tunnel service is online, it starts the Nginx service.
     * If no service is online, it stops the Nginx service.
     */
    public void checkAndManageNginxStatus() {
        // Get the list of all webTunnels and Onion services
        List<String> allServices = nginxService.getAllOnionAndWebTunnelServices();

        // Iterate over the list and check the status of each service
        for (String service : allServices) {
            String status = relayStatusService.getRelayStatus(service, "onion");
            // If at least one service is online, start the Nginx service and return
            if ("online".equals(status)) {
                nginxService.startNginx();
                return;
            }
        }

        // If no service is online, stop the Nginx service
        nginxService.stopNginx();
    }
}