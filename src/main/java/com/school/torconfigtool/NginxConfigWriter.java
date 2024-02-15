// NginxConfigWriter.java
package com.school.torconfigtool;

import com.school.torconfigtool.nginx.service.NginxConfigGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class NginxConfigWriter {

    private static final Logger logger = LoggerFactory.getLogger(NginxConfigWriter.class);

    private final NginxConfigGenerator nginxConfigGenerator;
    private final NginxFileService nginxFileService;
    private final NginxServiceManager nginxServiceManager;

    @Autowired
    public NginxConfigWriter(NginxConfigGenerator nginxConfigGenerator, NginxFileService nginxFileService, NginxServiceManager nginxServiceManager) {
        this.nginxConfigGenerator = nginxConfigGenerator;
        this.nginxFileService = nginxFileService;
        this.nginxServiceManager = nginxServiceManager;
    }

    public void writeCommonConfig(String rootDirectory) {
        Path defaultConfigPath = Paths.get("/etc/nginx/sites-available/default");
        List<String> lines = nginxConfigGenerator.generateCommonConfig(rootDirectory, "80", null, null);
        writeConfigAndRestartService(defaultConfigPath, lines);
    }

    public void writeInitialConfig(Path configPath, String programLocation) throws IOException {
        List<String> lines = nginxConfigGenerator.generateCommonConfig(programLocation + "/onion/www/service-80", "80", null, null);
        writeConfigAndRestartService(configPath, lines);
    }

    public void writeFinalConfig(Path configPath, String programLocation, String randomString) throws IOException {
        List<String> lines = nginxConfigGenerator.generateFinalConfigLines(programLocation, randomString);
        writeConfigAndRestartService(configPath, lines);
    }

    public void writeModifiedConfig(String programLocation, String randomString, String webTunnelUrl) {
        Path defaultConfigPath = Paths.get("/etc/nginx/sites-available/default");

        try {
            writeInitialConfig(defaultConfigPath, programLocation);
            nginxFileService.installCert(webTunnelUrl);
            writeFinalConfig(defaultConfigPath, programLocation, randomString);
        } catch (IOException e) {
            logger.error("Error modifying Nginx default configuration", e);
        }
    }

    public void writeConfigAndRestartService(Path configPath, List<String> lines) {
        try {
            Files.write(configPath, lines);
        } catch (IOException e) {
            logger.error("Error writing to Nginx configuration file", e);
        }
        nginxServiceManager.restartNginxService();
    }
}