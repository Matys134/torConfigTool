package com.school.torconfigtool.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NginxServiceManager implements StatusChangeListener {

    private final RelayStatusService relayStatusService;
    private final NginxService nginxService;

    public NginxServiceManager(RelayStatusService relayStatusService, NginxService nginxService) {
        this.relayStatusService = relayStatusService;
        this.nginxService = nginxService;
        this.relayStatusService.setStatusChangeListener(this);
    }

    @Override
    public void onStatusChange(String status) {
        checkAndManageNginxStatus();
    }

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