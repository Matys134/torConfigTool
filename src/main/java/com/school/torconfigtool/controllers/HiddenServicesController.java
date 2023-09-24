package com.school.torconfigtool.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/hidden-services")
public class HiddenServicesController {

    @GetMapping
    public String hiddenServicesConfigurationForm() {
        return "hidden-services-config"; // Thymeleaf template name (hidden-services-config.html)
    }

    @PostMapping("/configure")
    public String configureHiddenService(@RequestParam String serviceName,
                                         @RequestParam int servicePort,
                                         @RequestParam String serviceTarget,
                                         @RequestParam String servicePrivateKey,
                                         @RequestParam String serviceConfig,
                                         Model model) {
        // Handle form submission here
        // Update Hidden Services configuration with the provided data

        // Redirect back to the configuration form with a success message
        model.addAttribute("successMessage", "Hidden Service configured successfully!");
        return "hidden-services-config"; // Thymeleaf template name (hidden-services-config.html)
    }
}
