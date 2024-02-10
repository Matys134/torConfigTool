package com.school.torconfigtool.config;

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
                return process.pid();
            }
        } finally {
            process.destroy();
        }
        return -1;
    }

    public boolean stop(long pid) throws IOException, InterruptedException {
        LOGGER.info("Attempting to stop Tor process with PID: " + pid);
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "sudo kill -9 " + pid);
        processBuilder.redirectErrorStream(true); // Redirect stderr to stdout
        Process process = processBuilder.start();
        try {
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
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "pgrep -f 'tor -f " + filePath + "'");
        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = reader.readLine();
            if (line != null) {
                long pid = Long.parseLong(line);
                // Check if the process with the PID is still running
                ProcessBuilder checkProcessBuilder = new ProcessBuilder("/bin/bash", "-c", "ps -p " + pid);
                Process checkProcess = checkProcessBuilder.start();
                try (BufferedReader checkReader = new BufferedReader(new InputStreamReader(checkProcess.getInputStream()))) {
                    String checkLine = checkReader.readLine();
                    if (checkLine != null && checkLine.contains(String.valueOf(pid))) {
                        return pid;
                    }
                }
            }
        }
        return -1;
    }
}