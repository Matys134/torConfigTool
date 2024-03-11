package com.school.torconfigtool.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class ConfigController {

    @GetMapping("/config")
    public String getConfig() {
        try (Stream<String> lines = Files.lines(Paths.get("config.txt"))) {
            return lines.collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return "Error reading config file";
        }
    }
}