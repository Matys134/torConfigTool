// NginxConfigService.java
package com.school.torconfigtool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NginxConfigService {

    private final NginxConfigGenerator nginxConfigGenerator;
    private final NginxConfigModifier nginxConfigModifier;

    @Autowired
    public NginxConfigService(NginxConfigGenerator nginxConfigGenerator, NginxConfigModifier nginxConfigModifier) {
        this.nginxConfigGenerator = nginxConfigGenerator;
        this.nginxConfigModifier = nginxConfigModifier;
    }

    public void generateNginxConfig() {
        nginxConfigGenerator.generateNginxConfig();
    }

    public void changeRootDirectory(String rootDirectory) {
        nginxConfigModifier.changeRootDirectory(rootDirectory);
    }

    public void revertNginxDefaultConfig() {
        nginxConfigModifier.revertNginxDefaultConfig();
    }

    public void modifyNginxDefaultConfig(String programLocation, String randomString, String webTunnelUrl) {
        nginxConfigModifier.modifyNginxDefaultConfig(programLocation, randomString, webTunnelUrl);
    }
}