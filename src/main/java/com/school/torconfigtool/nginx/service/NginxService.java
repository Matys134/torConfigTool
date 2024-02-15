package com.school.torconfigtool.nginx.service;

import org.springframework.beans.factory.annotation.Autowired;
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

    public void reloadNginx() {
        nginxProcessService.reloadNginx();
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
}