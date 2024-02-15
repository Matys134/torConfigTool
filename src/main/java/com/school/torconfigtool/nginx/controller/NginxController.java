// NginxController.java
package com.school.torconfigtool.nginx.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * This class is a controller for handling requests related to Nginx.
 * It provides an endpoint for refreshing the Nginx configuration.
 */
@Controller
@RequestMapping("/nginx")
public class NginxController {

    // Logger instance for this class
    private static final Logger logger = LoggerFactory.getLogger(NginxController.class);

    /**
     * This method provides an endpoint for refreshing the Nginx configuration.
     * It executes a system command to reload the Nginx configuration.
     *
     * @return ResponseEntity<Void> - Returns a ResponseEntity with HTTP status code 200 (OK) if the operation was successful,
     *                                or with HTTP status code 500 (Internal Server Error) if an exception occurred.
     */
    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshNginx() {
        try {
            // Create a process builder with the system command to reload Nginx
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "systemctl", "reload", "nginx");

            // Start the process and wait for it to complete
            Process process = processBuilder.start();
            process.waitFor();

            // If the process completes successfully, return a ResponseEntity with HTTP status code 200 (OK)
            return ResponseEntity.ok().build();
        } catch (IOException | InterruptedException e) {
            // Log the exception and return a ResponseEntity with HTTP status code 500 (Internal Server Error)
            logger.error("Error refreshing Nginx", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}