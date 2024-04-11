package com.school.torconfigtool.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/setup")
public class SetupController {

    /**
     * Handles GET requests to the "/setup" endpoint.
     * Returns the setup view.
     *
     * @return the name of the setup view
     */
    @GetMapping
    public String setup() {
        return "setup";
    }
}
