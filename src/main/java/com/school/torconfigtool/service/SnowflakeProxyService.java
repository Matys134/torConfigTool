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
                System.err.println("Failed to create file: " + snowflakeProxyRunningFile.getAbsolutePath());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startSnowflakeProxy() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "systemctl", "start", "snowflake-proxy");
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopSnowflakeProxy() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "systemctl", "stop", "snowflake-proxy");
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // method to remove the snowflake proxy file
    public void removeSnowflakeProxy() {
        try {
            File snowflakeProxyRunningFile = new File(TORRC_DIRECTORY_PATH, "snowflake_proxy_running");
            if (!snowflakeProxyRunningFile.delete()) {
                System.err.println("Failed to delete file: " + snowflakeProxyRunningFile.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}