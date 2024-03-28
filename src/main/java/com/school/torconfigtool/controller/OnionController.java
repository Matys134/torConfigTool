package com.school.torconfigtool.controller;

import com.school.torconfigtool.model.TorConfig;
import com.school.torconfigtool.service.OnionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/onion-service")
public class OnionController {
    private final OnionService onionService;
    TorConfig torConfig = new TorConfig();

    @Autowired
    public OnionController(OnionService onionService) {
        this.onionService = onionService;
        initializeOnionController();
    }

    private void initializeOnionController() {

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
        String hostname = onionService.readHostnameFile(Integer.parseInt(torConfig.getHiddenServicePort()));

        model.addAttribute("hostname", hostname);
        model.addAttribute("hostnames", hostnames);

        return "setup";
    }

    @PostMapping("/configure")
    public String configureOnionService(@RequestParam int onionServicePort, @RequestParam String onionServiceNickname, Model model) {
        try {
            onionService.configureOnionService(onionServicePort, onionServiceNickname);
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