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

/**
 * OnionController is a Spring MVC Controller that handles operations related to Onion Services.
 */
@Controller
@RequestMapping("/onion-service")
public class OnionController {
    private final OnionService onionService;
    TorConfig torConfig = new TorConfig();

    /**
     * Constructor for OnionController.
     * @param onionService The service to handle onion operations.
     */
    @Autowired
    public OnionController(OnionService onionService) {
        this.onionService = onionService;
        initializeOnionController();
    }

    /**
     * Initializes the OnionController by creating the necessary directories if they do not exist.
     */
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

    /**
     * Handles the GET request to show the Onion Service configuration form.
     * @param model The Model object to be returned to the view.
     * @return The name of the view to be rendered.
     */
    @GetMapping
    public String onionServiceConfigurationForm(Model model) {
        Map<String, String> hostnames = onionService.getCurrentHostnames();
        String hostname = onionService.readHostnameFile(Integer.parseInt(torConfig.getOnionConfig().getHiddenServicePort()));

        model.addAttribute("hostname", hostname);
        model.addAttribute("hostnames", hostnames);

        return "setup";
    }

    /**
     * Handles the POST request to configure the Onion Service.
     * @param onionServicePort The port number for the Onion Service.
     * @param model The Model object to be returned to the view.
     * @return The name of the view to be rendered.
     */
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

    /**
     * Handles the POST request to start the Onion Service.
     * @param model The Model object to be returned to the view.
     * @return The name of the view to be rendered.
     */
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