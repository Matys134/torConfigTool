package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NginxService {

    private static final Logger logger = LoggerFactory.getLogger(NginxService.class);

    private final NginxProcessService nginxProcessService;
    private final NginxConfigService nginxConfigService;
    private final NginxFileService nginxFileService;

    @Autowired
    public NginxService(NginxProcessService nginxProcessService, NginxConfigService nginxConfigService, NginxFileService nginxFileService) {
        this.nginxProcessService = nginxProcessService;
        this.nginxConfigService = nginxConfigService;
        this.nginxFileService = nginxFileService;
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