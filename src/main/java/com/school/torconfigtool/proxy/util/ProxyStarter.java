package com.school.torconfigtool.proxy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class is responsible for starting and stopping a Tor process.
 */
public class ProxyStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyStarter.class);

    /**
     * Starts the Tor process with the specified configuration file.
     *
     * @param filePath The file path of the Tor configuration file.
     * @return The process ID of the Tor process if it is successfully started, or -1 if the process fails to start.
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the current thread is interrupted while waiting for the Tor process to start.
     */
    public long start(String filePath) throws IOException, InterruptedException {
        long pid = getRunningTorProcessId(filePath);
        if (pid != -1) {
            LOGGER.info("Tor process already running with PID: " + pid);
            return pid;
        }

        LOGGER.info("Attempting to start Tor process with command: sudo tor -f " + filePath);
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "sudo tor -f " + filePath);
        processBuilder.redirectErrorStream(true); // Redirect stderr to stdout
        Process process = processBuilder.start();
        try {
            LOGGER.info("Waiting for Tor process to complete...");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    LOGGER.info(line); // Log output of Tor process
                }
            }
            int exitCode = process.waitFor();
            LOGGER.info("Tor process completed with exit code " + exitCode);
            if (exitCode == 0) {
                pid = process.pid();
                LOGGER.info("Tor process started with PID: " + pid); // Log the PID immediately after the process starts
                return pid;
            }
        } finally {
            process.destroy();
        }
        return -1;
    }

    /**
     * Stops the Tor process with the specified configuration file.
     *
     * @param filePath The file path of the Tor configuration file.
     * @return true if the Tor process is successfully stopped, or false if the process fails to stop.
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the current thread is interrupted while waiting for the Tor process to stop.
     */
    public boolean stop(String filePath) throws IOException, InterruptedException {
        long pid = getRunningTorProcessId(filePath);
        if (pid == -1) {
            LOGGER.info("No running Tor process found with file path: " + filePath);
            return false;
        }

        LOGGER.info("Attempting to stop Tor process with PID: " + pid);
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "sudo kill " + pid);
        LOGGER.info("Command: " + processBuilder.command());
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    LOGGER.info(line); // Log output of kill command
                }
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    LOGGER.error(line); // Log error output of kill command
                }
            }
            int exitCode = process.waitFor();
            LOGGER.info("Tor process stop command completed with exit code " + exitCode);
            return exitCode == 0;
        } finally {
            process.destroy();
        }
    }

    /**
     * Gets the process ID of the running Tor process with the specified configuration file.
     *
     * @param filePath The file path of the Tor configuration file.
     * @return The process ID of the running Tor process if found, or -1 if no running Tor process is found.
     * @throws IOException if an I/O error occurs.
     */
    public long getRunningTorProcessId(String filePath) throws IOException {
        LOGGER.info("Checking if Tor process is already running...");
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "ps -ef | grep tor | grep " + filePath + " | grep -v grep | awk '{print $2}'");
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    long pid = Long.parseLong(line);
                    LOGGER.info("Tor process is already running with PID: " + pid);
                    return pid;
                }
            }
            return -1;
        } finally {
            process.destroy();
        }
    }
}