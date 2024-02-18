package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProxyStarter {

    /**
     * This class is responsible for starting and stopping a Tor process.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyStarter.class);

    /**
     * This method starts a Tor process with the specified configuration file.
     * If a Tor process is already running with the same configuration file, it returns the PID of the running process.
     * Otherwise, it attempts to start a new Tor process and returns its PID.
     * If the process fails to start, it returns -1.
     *
     * @param filePath The path of the configuration file to be used by the Tor process.
     * @return The PID of the started Tor process, or -1 if the process fails to start.
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException If the current thread is interrupted while waiting for the process to complete.
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
     * This method stops a running Tor process that was started with the specified configuration file.
     * If no such process is running, it returns false.
     * Otherwise, it attempts to stop the process and returns true if successful, or false if the process fails to stop.
     *
     * @param filePath The path of the configuration file used by the Tor process to be stopped.
     * @return True if the process is successfully stopped, or false otherwise.
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException If the current thread is interrupted while waiting for the process to complete.
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
     * This method checks if a Tor process is already running with the specified configuration file.
     * If such a process is running, it returns the PID of the process.
     * Otherwise, it returns -1.
     *
     * @param filePath The path of the configuration file to be checked.
     * @return The PID of the running Tor process, or -1 if no such process is running.
     * @throws IOException If an I/O error occurs.
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