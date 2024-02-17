// NginxController.java
package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/nginx")
public class NginxController {

    private static final Logger logger = LoggerFactory.getLogger(NginxController.class);

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshNginx() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "systemctl", "reload", "nginx");
            Process process = processBuilder.start();
            process.waitFor();
            return ResponseEntity.ok().build();
        } catch (IOException | InterruptedException e) {
            logger.error("Error refreshing Nginx", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}