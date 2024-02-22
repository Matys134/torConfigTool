package com.school.torconfigtool.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * Service class for running the Snowflake proxy.
 */
@Service
public class SnowflakeProxyService {
    private static final Logger logger = LoggerFactory.getLogger(SnowflakeProxyService.class);
    private static final String TORRC_DIRECTORY_PATH = "torrc/";

    /**
     * Runs the Snowflake proxy.
     */
    public void setupSnowflakeProxy() {
        try {
            File snowflakeProxyRunningFile = new File(TORRC_DIRECTORY_PATH, "snowflake_proxy_running");
            if (!snowflakeProxyRunningFile.createNewFile()) {
                logger.error("Failed to create file: " + snowflakeProxyRunningFile.getAbsolutePath());
            }

        } catch (IOException e) {
            logger.error("Error running snowflake proxy", e);
        }
    }

    public void startSnowflakeProxy() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "systemctl", "start", "snowflake-proxy");
            processBuilder.start();
        } catch (IOException e) {
            logger.error("Error starting Snowflake proxy", e);
        }
    }

    public void stopSnowflakeProxy() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "systemctl", "stop", "snowflake-proxy");
            processBuilder.start();
        } catch (IOException e) {
            logger.error("Error stopping Snowflake proxy", e);
        }
    }

    // method to remove the snowflake proxy file
    public void removeSnowflakeProxy() {
        try {
            File snowflakeProxyRunningFile = new File(TORRC_DIRECTORY_PATH, "snowflake_proxy_running");
            if (!snowflakeProxyRunningFile.delete()) {
                logger.error("Failed to delete file: " + snowflakeProxyRunningFile.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.error("Error removing snowflake proxy", e);
        }
    }
}