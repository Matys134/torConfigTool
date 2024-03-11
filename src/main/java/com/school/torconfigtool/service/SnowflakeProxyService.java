package com.school.torconfigtool.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

import static com.school.torconfigtool.util.Constants.TORRC_DIRECTORY_PATH;

/**
 * Service class for running the Snowflake proxy.
 */
@Service
public class SnowflakeProxyService {

    /**
     * Runs the Snowflake proxy.
     */
    public void setupSnowflakeProxy() {
        try {
            File snowflakeProxyRunningFile = new File(TORRC_DIRECTORY_PATH, "snowflake_proxy_running");
            if (!snowflakeProxyRunningFile.createNewFile()) {
                throw new IOException("Failed to create file: " + snowflakeProxyRunningFile.getAbsolutePath());
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to create snowflake_proxy_running file", e);
        }
    }

    public void startSnowflakeProxy() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "systemctl", "start", "snowflake-proxy");
            processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to start snowflake proxy", e);
        }
    }

    public void stopSnowflakeProxy() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "systemctl", "stop", "snowflake-proxy");
            processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to stop snowflake proxy", e);
        }
    }

    // method to remove the snowflake proxy file
    public void removeSnowflakeProxy() {
        try {
            File snowflakeProxyRunningFile = new File(TORRC_DIRECTORY_PATH, "snowflake_proxy_running");
            if (!snowflakeProxyRunningFile.delete()) {
                throw new IOException("Failed to remove file: " + snowflakeProxyRunningFile.getAbsolutePath());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove snowflake_proxy_running file", e);
        }
    }
}