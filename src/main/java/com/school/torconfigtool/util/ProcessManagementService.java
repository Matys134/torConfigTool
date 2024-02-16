package com.school.torconfigtool.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing processes.
 */
@Service
public class ProcessManagementService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessManagementService.class);

    /**
     * Class to hold the result of a command execution.
     */
    private static class CommandResult {
        List<String> outputLines;
        int exitCode;
    }

    /**
     * Executes a command and returns the output and exit code.
     *
     * @param command The command to execute.
     * @return The result of the command execution.
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException If the current thread is interrupted.
     */
    private CommandResult executeCommandWithOutput(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        Process process = processBuilder.start();

        List<String> outputLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputLines.add(line);
            }
        }

        int exitCode = process.waitFor();
        process.destroy();

        CommandResult result = new CommandResult();
        result.outputLines = outputLines;
        result.exitCode = exitCode;
        return result;
    }

    /**
     * Executes a command and returns the output.
     *
     * @param command The command to execute.
     * @return The output of the command.
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException If the current thread is interrupted.
     */
    public List<String> getCommandOutput(String command) throws IOException, InterruptedException {
        CommandResult result = executeCommandWithOutput(command);
        return result.outputLines;
    }

    /**
     * Executes a command and returns the exit code.
     *
     * @param command The command to execute.
     * @return The exit code of the command.
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException If the current thread is interrupted.
     */
    public int executeCommand(String command) throws IOException, InterruptedException {
        CommandResult result = executeCommandWithOutput(command);
        return result.exitCode;
    }

    /**
     * Gets the PID of a Tor relay.
     *
     * @param torrcFilePath The path to the Tor configuration file.
     * @return The PID of the Tor relay, or -1 if not found.
     */
    public int getTorRelayPID(String torrcFilePath) {
        String relayNickname = new File(torrcFilePath).getName();
        String command = String.format("ps aux | grep -P '\\b%s\\b' | grep -v grep | awk '{print $2}'", relayNickname);

        try {
            List<String> outputLines = getCommandOutput(command);

            if (!outputLines.isEmpty()) {
                String pidString = outputLines.getFirst();
                return Integer.parseInt(pidString);
            } else {
                logger.debug("No PID found. Output was empty.");
                return -1;
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error executing command to get PID: {}", command, e);
            return -1;
        }
    }
}