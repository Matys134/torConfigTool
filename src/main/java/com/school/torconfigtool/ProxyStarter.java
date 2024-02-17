package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProxyStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyStarter.class);

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