package com.school.torconfigtool.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProcessManagementService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessManagementService.class);

    public int executeCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        Process process = processBuilder.start();
        System.out.println(command);

        try {
            return process.waitFor();
        } finally {
            process.destroy();
        }
    }

    public int getTorRelayPID(String torrcFilePath) {
        String relayNickname = new File(torrcFilePath).getName();
        String command = String.format("ps aux | grep -P '\\b%s\\b' | grep -v grep | awk '{print $2}'", relayNickname);

        System.out.println(command);
        logger.debug("Command to execute: {}", command);

        try {
            List<String> outputLines = getStrings(command);

            // Log the full output
            logger.debug("Command output: {}", outputLines);

            // Assuming the PID is on the first line, if not you need to check the outputLines list.
            if (!outputLines.isEmpty()) {
                String pidString = outputLines.get(0);
                logger.debug("PID string: {}", pidString);
                return Integer.parseInt(pidString);
            } else {
                logger.debug("No PID found. Output was empty.");
                return -1;
            }
        } catch (IOException e) {
            logger.error("Error executing command to get PID: {}", command, e);
            return -1;
        }
    }

    private static List<String> getStrings(String command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        Process process = processBuilder.start();

        // Read the entire output to ensure we're not missing anything.
        List<String> outputLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputLines.add(line);
            }
        }
        return outputLines;
    }


}
