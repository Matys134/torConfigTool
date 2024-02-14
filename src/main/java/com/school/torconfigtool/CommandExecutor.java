package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CommandExecutor {
    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    public static Process executeCommand(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("Error during command execution. Exit code: " + exitCode);
            }
            return process;
        } catch (IOException | InterruptedException e) {
            logger.error("Error during command execution", e);
            return null;
        }
    }
}