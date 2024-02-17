package com.school.torconfigtool;

import com.school.torconfigtool.nginx.service.NginxConfigService;
import com.school.torconfigtool.nginx.service.NginxProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class NginxService {

    private final NginxProcessService nginxProcessService;
    private final NginxConfigService nginxConfigService;

    @Autowired
    public NginxService(NginxProcessService nginxProcessService, NginxConfigService nginxConfigService) {
        this.nginxProcessService = nginxProcessService;
        this.nginxConfigService = nginxConfigService;
    }

    public void startNginx() {
        nginxProcessService.startNginx();
    }

    public void generateNginxConfig() {
        nginxConfigService.generateNginxConfig();
    }

    public void changeRootDirectory(String rootDirectory) {
        nginxConfigService.changeRootDirectory(rootDirectory);
    }

    public void revertNginxDefaultConfig() {
        nginxConfigService.revertNginxDefaultConfig();
    }

    public void modifyNginxDefaultConfig(String programLocation, String randomString, String webTunnelUrl) {
        nginxConfigService.modifyNginxDefaultConfig(programLocation, randomString, webTunnelUrl);
    }

    public boolean isNginxRunning() {
        return nginxProcessService.isNginxRunning();
    }

    @EventListener
    public void handleNginxReloadEvent(NginxReloadEvent event) {
        reloadNginx();
    }

    public void reloadNginx() {
        nginxProcessService.reloadNginx();
    }
}