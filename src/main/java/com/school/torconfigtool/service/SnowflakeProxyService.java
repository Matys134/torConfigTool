package com.school.torconfigtool.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

import static com.school.torconfigtool.util.Constants.TORRC_DIRECTORY_PATH;

/**
 * This service class is responsible for managing the Snowflake proxy.
 * It provides methods to setup, start, stop, and remove the Snowflake proxy.
 */
@Service
public class SnowflakeProxyService {

    /**
     * This method sets up the Snowflake proxy.
     * It creates a new file named "snowflake_proxy_configured" in the TORRC_DIRECTORY_PATH.
     * If the file creation fails, it throws an IOException.
     */
    public void setupSnowflakeProxy() {
        try {
            File snowflakeProxyRunningFile = new File(TORRC_DIRECTORY_PATH, "snowflake_proxy_configured");
            if (!snowflakeProxyRunningFile.createNewFile()) {
                throw new IOException("Failed to create file: " + snowflakeProxyRunningFile.getAbsolutePath());
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to create snowflake_proxy_configured file", e);
        }
    }

    /**
     * This method starts the Snowflake proxy.
     * It uses a ProcessBuilder to execute the command "sudo systemctl start snowflake-proxy".
     * If the command execution fails, it throws an IOException.
     */
    public void startSnowflakeProxy() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "systemctl", "start", "snowflake-proxy");
            processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to start snowflake proxy", e);
        }
    }

    /**
     * This method stops the Snowflake proxy.
     * It uses a ProcessBuilder to execute the command "sudo systemctl stop snowflake-proxy".
     * If the command execution fails, it throws an IOException.
     */
    public void stopSnowflakeProxy() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "systemctl", "stop", "snowflake-proxy");
            processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to stop snowflake proxy", e);
        }
    }

    /**
     * This method removes the Snowflake proxy.
     * It deletes the file named "snowflake_proxy_configured" from the TORRC_DIRECTORY_PATH.
     * If the file deletion fails, it throws an IOException.
     */
    public void removeSnowflakeProxy() {
        try {
            File snowflakeProxyRunningFile = new File(TORRC_DIRECTORY_PATH, "snowflake_proxy_configured");
            if (!snowflakeProxyRunningFile.delete()) {
                throw new IOException("Failed to remove file: " + snowflakeProxyRunningFile.getAbsolutePath());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove snowflake_proxy_configured file", e);
        }
    }
}