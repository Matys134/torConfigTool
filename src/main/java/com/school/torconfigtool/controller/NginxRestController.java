package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.NginxService;
import com.school.torconfigtool.service.OnionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/nginx-api")
public class NginxRestController {

    private final NginxService nginxService;
    private final OnionService onionService;

    @Autowired
    public NginxRestController(NginxService nginxService, OnionService onionService) {
        this.nginxService = nginxService;
        this.onionService = onionService;
    }

    @PostMapping("/revert-nginx-config")
    public ResponseEntity<String> revertNginxConfig() {
        try {
            nginxService.revertNginxDefaultConfig();
            return new ResponseEntity<>("Nginx configuration reverted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error reverting Nginx configuration: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/refresh-nginx")
    public ResponseEntity<Void> refreshNginx() {
        try {
            onionService.refreshNginx();
            return ResponseEntity.ok().build();
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}