package com.school.torconfigtool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NginxConfigManager {

    private final NginxConfigWriter nginxConfigWriter;

    @Autowired
    public NginxConfigManager(NginxConfigWriter nginxConfigWriter) {
        this.nginxConfigWriter = nginxConfigWriter;
    }

    public void changeRootDirectory(String rootDirectory) {
        nginxConfigWriter.writeCommonConfig(rootDirectory);
    }

    public void revertNginxDefaultConfig() {
        String rootDirectory = "/home/matys/git/torConfigTool/onion/www/service-80";
        nginxConfigWriter.writeCommonConfig(rootDirectory);
    }

    public void modifyNginxDefaultConfig(String programLocation, String randomString, String webTunnelUrl) {
        nginxConfigWriter.writeModifiedConfig(programLocation, randomString, webTunnelUrl);
    }
}