package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.OnionService;
import com.school.torconfigtool.model.TorConfig;
import com.school.torconfigtool.service.TorConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/onion-service")
public class OnionController {
    private final TorConfigService torConfigService;
    private final OnionService onionService;
    TorConfig torConfig = new TorConfig();

    @Autowired
    public OnionController(TorConfigService torConfigService, OnionService onionService) {
        this.torConfigService = torConfigService;
        this.onionService = onionService;
        initializeOnionController();
    }

    private void initializeOnionController() {
        /*
        List<String> onionServicePorts = onionService.getAllOnionServicePorts();

        if (!onionServicePorts.isEmpty()) {
            torConfig.setHiddenServicePort(onionServicePorts.getFirst());
        }*/

        String hiddenServiceDirsPath = System.getProperty("user.dir") + "/onion/hiddenServiceDirs";
        File hiddenServiceDirs = new File(hiddenServiceDirsPath);
        if (!hiddenServiceDirs.exists()) {
            boolean dirCreated = hiddenServiceDirs.mkdirs();
            if (!dirCreated) {
                throw new RuntimeException("Failed to create hiddenServiceDirs directory.");
            }
        }
    }

    @GetMapping
    public String onionServiceConfigurationForm(Model model) {
        Map<String, String> hostnames = onionService.getCurrentHostnames();
        List<TorConfig> onionConfigs = torConfigService.readTorConfigurations();
        String hostname = onionService.readHostnameFile(Integer.parseInt(torConfig.getHiddenServicePort()));

        model.addAttribute("hostname", hostname);
        model.addAttribute("onionConfigs", onionConfigs);
        model.addAttribute("hostnames", hostnames);

        return "setup";
    }

    @PostMapping("/configure")
    public String configureOnionService(@RequestParam int onionServicePort, Model model) {
        try {
            onionService.configureOnionService(onionServicePort);
            model.addAttribute("successMessage", "Tor Onion Service configured successfully!");
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Failed to configure Tor Onion Service.");
        }
        return "setup";
    }

    @PostMapping("/start")
    public String startOnionService(Model model) {
        boolean startSuccess = onionService.startTorOnion();
        if (startSuccess) {
            model.addAttribute("successMessage", "Tor Onion Service started successfully!");
        } else {
            model.addAttribute("errorMessage", "Failed to start Tor Onion Service.");
        }
        return "setup";
    }
}