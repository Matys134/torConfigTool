package com.school.torconfigtool.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class ProcessManagementService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessManagementService.class);

    public int executeCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        Process process = processBuilder.start();

        try {
            return process.waitFor();
        } finally {
            process.destroy();
        }
    }

    public int getTorRelayPID(String torrcFilePath) {
        String relayNickname = new File(torrcFilePath).getName();
        String command = String.format("ps aux | grep %s | grep -v grep | awk '{print $2}'", relayNickname);

        try {
            return executeCommandAndGetPid(command);
        } catch (IOException | InterruptedException e) {
            logger.error("Error executing command to get PID: {}", command, e);
            return -1;
        }
    }

    private int executeCommandAndGetPid(String command) throws IOException, InterruptedException {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String pidString = reader.readLine();
                return pidString != null && !pidString.isEmpty() ? Integer.parseInt(pidString) : -1;
            }
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }
}
