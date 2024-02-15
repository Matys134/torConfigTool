package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class NginxProcessService {

    private static final Logger logger = LoggerFactory.getLogger(NginxProcessService.class);

    public void startNginx() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "sudo systemctl start nginx");
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("Error starting Nginx. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error starting Nginx", e);
        }
    }

    public void reloadNginx() {
        System.out.println("Reloading Nginx");
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "sudo systemctl reload nginx");
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("Error reloading Nginx. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error reloading Nginx", e);
        }
    }

    public boolean isNginxRunning() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "systemctl is-active nginx");
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            logger.error("Error checking Nginx status", e);
            return false;
        }
    }
}