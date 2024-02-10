package com.school.torconfigtool.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProxyStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyStarter.class);

    public long start(String filePath) throws IOException, InterruptedException {
        long pid = getRunningTorProcessId();
        if (pid != -1) {
            return pid;
        }

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
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "sudo kill " + pid);
        Process process = processBuilder.start();
        try {
            int exitCode = process.waitFor();
            return exitCode == 0;
        } finally {
            process.destroy();
        }
    }

    public long getRunningTorProcessId() throws IOException { // Change this line
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "pgrep tor");
        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = reader.readLine();
            if (line != null) {
                return Long.parseLong(line);
            }
        }
        return -1;
    }
}