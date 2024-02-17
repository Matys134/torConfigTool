// NginxConfigService.java
package com.school.torconfigtool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NginxConfigService {

    private final NginxConfigGenerator nginxConfigGenerator;
    private final NginxConfigManager nginxConfigManager;

    @Autowired
    public NginxConfigService(NginxConfigGenerator nginxConfigGenerator, NginxConfigManager nginxConfigManager) {
        this.nginxConfigGenerator = nginxConfigGenerator;
        this.nginxConfigManager = nginxConfigManager;
    }

    public void generateNginxConfig() {
        nginxConfigGenerator.generateNginxConfig();
    }

    public void changeRootDirectory(String rootDirectory) {
        nginxConfigManager.changeRootDirectory(rootDirectory);
    }

    public void revertNginxDefaultConfig() {
        nginxConfigManager.revertNginxDefaultConfig();
    }

    public void modifyNginxDefaultConfig(String programLocation, String randomString, String webTunnelUrl) {
        nginxConfigManager.modifyNginxDefaultConfig(programLocation, randomString, webTunnelUrl);
    }
}