package com.school.torconfigtool.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GraphController {
    @GetMapping("/graph")
    public String showGraphPage() {
        return "graph"; // Thymeleaf template name (graph.html)
    }
}
