package com.school.torconfigtool.nginx.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Service class for managing Nginx processes.
 */
@Service
public class NginxProcessService {

    private static final Logger logger = LoggerFactory.getLogger(NginxProcessService.class);

    /**
     * Executes a bash command.
     *
     * @param command The command to execute.
     * @return The exit code of the command. Returns -1 if an error occurs during execution.
     */
    private int executeCommand(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);
        try {
            Process process = processBuilder.start();
            return process.waitFor();
        } catch (IOException | InterruptedException e) {
            logger.error("Error executing command: " + command, e);
            return -1;
        }
    }

    /**
     * Starts the Nginx service.
     * Logs an error if the service fails to start.
     */
    public void startNginx() {
        int exitCode = executeCommand("sudo systemctl start nginx");
        if (exitCode != 0) {
            logger.error("Error starting Nginx. Exit code: " + exitCode);
        }
    }

    /**
     * Reloads the Nginx service.
     * Logs an error if the service fails to reload.
     */
    public void reloadNginx() {
        int exitCode = executeCommand("sudo systemctl reload nginx");
        if (exitCode != 0) {
            logger.error("Error reloading Nginx. Exit code: " + exitCode);
        }
    }

    /**
     * Checks if the Nginx service is running.
     *
     * @return True if the service is running, false otherwise.
     */
    public boolean isNginxRunning() {
        int exitCode = executeCommand("systemctl is-active nginx");
        if (exitCode != 0) {
            logger.error("Error checking Nginx status. Exit code: " + exitCode);
            return false;
        }
        return true;
    }
}