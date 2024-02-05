package com.school.torconfigtool.service;

import com.school.torconfigtool.controllers.RelayOperationsController;
import com.simtechdata.waifupnp.UPnP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProcessManagementService {
    private final RelayOperationsController relayOperationsController;

    // Keep track of all ORPorts that were opened by the application
    private final Set<Integer> openedORPorts = new HashSet<>();

    @Autowired
    public ProcessManagementService(RelayOperationsController relayOperationsController) {
        this.relayOperationsController = relayOperationsController;
    }

    private static final Logger logger = LoggerFactory.getLogger(ProcessManagementService.class);

    private static List<String> getCommandOutput(String command) throws IOException {
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

    public int executeCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        Process process = processBuilder.start();

        // Log the command being executed
        logger.info("Executing command: {}", command);

        try {
            int exitCode = process.waitFor();

            // Log the exit code
            logger.info("Command exit code: {}", exitCode);

            return exitCode;
        } finally {
            process.destroy();
        }
    }

    public int getTorRelayPID(String torrcFilePath) {
        String relayNickname = new File(torrcFilePath).getName();
        String command = String.format("ps aux | grep -P '\\b%s\\b' | grep -v grep | awk '{print $2}'", relayNickname);

        // Log the command to be executed
        logger.debug("Command to execute: {}", command);

        try {
            List<String> outputLines = getCommandOutput(command);

            // Log the full output
            logger.debug("Command output: {}", outputLines);

            // Assuming the PID is on the first line, if not you need to check the outputLines list.
            if (!outputLines.isEmpty()) {
                String pidString = outputLines.getFirst();
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

    public void startUPnP() {
        // Iterate over all relays
        for (String relayNickname : relayOperationsController.getAllServices()) {
            // Check the status of the relay
            String status = relayOperationsController.getRelayStatus(relayNickname, "onion");

            // If the relay is online, open its ORPort using UPnP
            if ("online".equals(status)) {
                int orPort = relayOperationsController.getOrPort(relayOperationsController.buildTorrcFilePath(relayNickname, "onion"));
                boolean success = UPnP.openPortTCP(orPort);

                // If the ORPort was opened successfully, add it to the set of opened ORPorts
                if (success) {
                    openedORPorts.add(orPort);
                }
            }
        }
    }

    public void stopUPnP() {
        // Close all ORPorts that were opened by the application
        for (int orPort : openedORPorts) {
            UPnP.closePortTCP(orPort);
        }

        // Clear the set of opened ORPorts
        openedORPorts.clear();
    }
}
