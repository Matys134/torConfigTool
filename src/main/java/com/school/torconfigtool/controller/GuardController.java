package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.GuardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * This is a Spring MVC Controller for handling requests related to Tor Guard configuration.
 * It uses GuardService to perform the actual configuration tasks.
 */
@Controller
@RequestMapping("/guard")
public class GuardController {

    private final GuardService guardService;

    /**
     * Constructor for GuardController.
     * @param guardService The service to be used for configuring the Tor Guard.
     */
    public GuardController(GuardService guardService) {
        this.guardService = guardService;
    }

    /**
     * Handles GET requests to "/guard".
     * @return The name of the view to be rendered, in this case "setup".
     */
    @GetMapping
    public String guardConfigurationForm() {
        return "setup";
    }

    /**
     * Handles POST requests to "/guard/configure".
     * @param relayNickname The nickname of the relay.
     * @param relayPort The port of the relay.
     * @param relayContact The contact information for the relay.
     * @param controlPort The control port for the relay.
     * @param relayBandwidth The bandwidth for the guard. This is optional.
     * @param model The Model object to be used for adding attributes to the view.
     * @return The name of the view to be rendered, in this case "setup".
     */
    @PostMapping("/configure")
    public String configureGuard(@RequestParam String relayNickname,
                                 @RequestParam int relayPort,
                                 @RequestParam String relayContact,
                                 @RequestParam int controlPort,
                                 @RequestParam Integer relayBandwidth,
                                 Model model) {
        try {
            guardService.configureGuard(relayNickname, relayPort, relayContact, controlPort, relayBandwidth);
            model.addAttribute("successMessage", "Tor Relay configured successfully!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to configure Tor Relay: " + e.getMessage());
        }
        return "setup";
    }
}