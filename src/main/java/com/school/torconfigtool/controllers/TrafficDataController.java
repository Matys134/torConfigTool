package com.school.torconfigtool.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TrafficDataController {

    @GetMapping("/traffic-data")
    public String getTrafficDataPage() {
        return "data"; // Return the name of your HTML page
    }
}
