package com.school.torconfigtool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This class is used to execute bash commands.
 */
public class CommandExecutor {
    // Logger instance for this class
    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    /**
     * Executes a bash command and returns the resulting process.
     * If the command execution fails, it logs the error and returns null.
     *
     * @param command The bash command to execute
     * @return The resulting Process, or null if the command execution failed
     */
    public static Process executeCommand(String command) {
        // Create a new process builder
        ProcessBuilder processBuilder = new ProcessBuilder();
        // Set the command for the process builder
        processBuilder.command("bash", "-c", command);
        try {
            // Start the process
            Process process = processBuilder.start();
            // Wait for the process to finish and get the exit code
            int exitCode = process.waitFor();
            // If the exit code is not 0, log an error
            if (exitCode != 0) {
                logger.error("Error during command execution. Exit code: " + exitCode);
            }
            // Return the process
            return process;
        } catch (IOException | InterruptedException e) {
            // Log any exceptions that occur during command execution
            logger.error("Error during command execution", e);
            // Return null if an exception occurred
            return null;
        }
    }
}