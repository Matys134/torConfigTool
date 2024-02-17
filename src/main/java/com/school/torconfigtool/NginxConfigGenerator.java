// NginxConfigGenerator.java
package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class NginxConfigGenerator {

    private static final Logger logger = LoggerFactory.getLogger(NginxConfigGenerator.class);

    private final NginxFileService nginxFileService;

    @Autowired
    public NginxConfigGenerator(NginxFileService nginxFileService) {
        this.nginxFileService = nginxFileService;
    }

    public void generateNginxConfig() {
        try {
            String currentDirectory = System.getProperty("user.dir");
            File indexHtml = nginxFileService.getFile(currentDirectory);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(indexHtml))) {
                writer.write("<html><body><h1>Test Onion Service</h1></body></html>");
            }
        } catch (IOException e) {
            logger.error("Error generating Nginx configuration", e);
        }
    }
}