package com.school.torconfigtool;

import com.school.torconfigtool.nginx.service.NginxConfigGenerator;
import com.school.torconfigtool.nginx.service.NginxFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Service class responsible for writing Nginx configuration files.
 */
@Service
public class NginxConfigWriter {

    private static final Logger logger = LoggerFactory.getLogger(NginxConfigWriter.class);

    private final NginxConfigGenerator nginxConfigGenerator;
    private final NginxFileService nginxFileService;

    @Autowired
    public NginxConfigWriter(NginxConfigGenerator nginxConfigGenerator, NginxFileService nginxFileService) {
        this.nginxConfigGenerator = nginxConfigGenerator;
        this.nginxFileService = nginxFileService;
    }

    /**
     * Writes a common Nginx configuration to the default config path.
     *
     * @param rootDirectory the root directory for the Nginx service
     */
    public void writeCommonConfig(String rootDirectory) {
        Path defaultConfigPath = Paths.get("/etc/nginx/sites-available/default");
        List<String> lines = nginxConfigGenerator.generateCommonConfig(rootDirectory, "80", null, null);
        writeConfigAndRestartService(defaultConfigPath, lines);
    }

    /**
     * Writes an initial Nginx configuration to the specified path.
     *
     * @param configPath the path to write the configuration to
     * @param programLocation the location of the program
     * @throws IOException if an I/O error occurs
     */
    public void writeInitialConfig(Path configPath, String programLocation) throws IOException {
        List<String> lines = nginxConfigGenerator.generateCommonConfig(programLocation + "/onion/www/service-80", "80", null, null);
        writeConfigAndRestartService(configPath, lines);
    }

    /**
     * Writes a final Nginx configuration to the specified path.
     *
     * @param configPath the path to write the configuration to
     * @param programLocation the location of the program
     * @param randomString a random string to be used in the configuration
     * @throws IOException if an I/O error occurs
     */
    public void writeFinalConfig(Path configPath, String programLocation, String randomString) throws IOException {
        List<String> lines = nginxConfigGenerator.generateFinalConfigLines(programLocation, randomString);
        writeConfigAndRestartService(configPath, lines);
    }

    /**
     * Writes a modified Nginx default configuration.
     *
     * @param programLocation the location of the program
     * @param randomString a random string to be used in the configuration
     * @param webTunnelUrl the web tunnel URL
     */
    public void writeModifiedConfig(String programLocation, String randomString, String webTunnelUrl) {
        Path defaultConfigPath = Paths.get("/etc/nginx/sites-available/default");

        try {
            writeInitialConfig(defaultConfigPath, programLocation);
            nginxFileService.installCert(webTunnelUrl);
            writeFinalConfig(defaultConfigPath, programLocation, randomString);
        } catch (IOException e) {
            logger.error("Error modifying Nginx default configuration", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes a configuration to the specified path and restarts the Nginx service.
     *
     * @param configPath the path to write the configuration to
     * @param lines the lines of the configuration
     */
    public void writeConfigAndRestartService(Path configPath, List<String> lines) {
        try {
            Files.write(configPath, lines);
        } catch (IOException e) {
            logger.error("Error writing to Nginx configuration file", e);
        }
    }
}