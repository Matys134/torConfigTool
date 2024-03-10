package com.school.torconfigtool.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@RestController
public class ConfigController {

    @GetMapping("/config")
    public String getConfig() {
        try {
            return Files.lines(Paths.get("config.txt")).collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reading config file";
        }
    }
}