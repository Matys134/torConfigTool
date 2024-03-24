package com.school.torconfigtool.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is a Rest Controller class for handling configuration related requests.
 */
@RestController
public class ConfigRestController {

    /**
     * This method is mapped to the "/config" endpoint.
     * It reads the content of the "config.txt" file and returns it as a string.
     * Each line in the file is separated by a newline character ("\n").
     * If there is an error reading the file, it returns a string "Error reading config file".
     *
     * @return A string containing the content of the "config.txt" file or an error message.
     */
    @GetMapping("/config")
    public String getConfig() {
        try (Stream<String> lines = Files.lines(Paths.get("config.txt"))) {
            return lines.collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return "Error reading config file";
        }
    }
}