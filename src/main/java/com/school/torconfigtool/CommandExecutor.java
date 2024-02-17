package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * This class is responsible for executing shell commands.
 * It is annotated with @Service to indicate that it's a service component in Spring framework.
 */
@Service
public class CommandExecutor {
    // Logger instance for this class
    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    /**
     * Executes a shell command and returns the resulting process.
     * If the command execution fails, it logs the error and throws an IOException.
     *
     * @param command The shell command to execute
     * @return The resulting Process after command execution
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the current thread is interrupted while waiting for the process to end
     */
    public Process executeCommand(String command) throws IOException, InterruptedException {
        // Create a new process builder
        ProcessBuilder processBuilder = new ProcessBuilder();

        // Set the command for the process builder to execute
        processBuilder.command("bash", "-c", command);

        // Start the process and wait for it to end
        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        // If the exit code is not 0, log the error and throw an IOException
        if (exitCode != 0) {
            logger.error("Error during command execution. Command: {}, Exit code: {}", command, exitCode);
            throw new IOException("Error during command execution. Exit code: " + exitCode);
        }

        // Return the resulting process
        return process;
    }
}