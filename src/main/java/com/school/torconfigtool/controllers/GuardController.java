package com.school.torconfigtool.controllers;

import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/guard")
public class GuardController {

    @GetMapping
    public String showGuardConfigurations(Model model) {
        List<GuardConfiguration> guardConfigurations = readGuardConfigurations();
        model.addAttribute("guardConfigurations", guardConfigurations);
        return "guard-config";
    }

    private List<GuardConfiguration> readGuardConfigurations() {
        List<GuardConfiguration> configurations = new ArrayList<>();
        File guardFolder = new File("torrc/guard");

        if (guardFolder.isDirectory()) {
            File[] files = guardFolder.listFiles((dir, name) -> name.endsWith(".torrc"));

            if (files != null) {
                for (File file : files) {
                    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                        String line;
                        GuardConfiguration config = new GuardConfiguration();

                        while ((line = br.readLine()) != null) {
                            if (line.startsWith("Nickname")) {
                                config.setNickname(line.substring(8).trim());
                            } else if (line.startsWith("ORPort")) {
                                config.setOrPort(line.substring(6).trim());
                            } else if (line.startsWith("Contact")) {
                                config.setContact(line.substring(7).trim());
                            }
                        }

                        configurations.add(config);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return configurations;
    }
}

@Getter
class GuardConfiguration {
    private String nickname;
    private String orPort;
    private String contact;

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setOrPort(String orPort) {
        this.orPort = orPort;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
