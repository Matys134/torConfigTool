package com.school.torconfigtool.controllers;

import com.school.torconfigtool.models.TorConfiguration;
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
@RequestMapping("/relay-operations")
public class RelayOperationsController {

    @GetMapping
    public String relayOperations(Model model) {
        List<TorConfiguration> guardConfigs = readTorConfigurations("torrc/guard");
        List<TorConfiguration> onionConfigs = readTorConfigurations("torrc/onion");

        model.addAttribute("guardConfigs", guardConfigs);
        model.addAttribute("onionConfigs", onionConfigs);

        return "relay-operations"; // Thymeleaf template name
    }

    private List<TorConfiguration> readTorConfigurations(String folder) {
        List<TorConfiguration> configs = new ArrayList<>();
        File[] files = new File(folder).listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        TorConfiguration config = new TorConfiguration();

                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("Nickname")) {
                                config.setNickname(line.split("Nickname")[1].trim());
                            } else if (line.startsWith("ORPort")) {
                                config.setOrPort(line.split("ORPort")[1].trim());
                            } else if (line.startsWith("Contact")) {
                                config.setContact(line.split("Contact")[1].trim());
                            } else if (line.startsWith("HiddenServiceDir")) {
                                // Onion service specific
                                config.setHiddenServiceDir(line.split("HiddenServiceDir")[1].trim());
                            } else if (line.startsWith("HiddenServicePort")) {
                                // Onion service specific
                                config.setHiddenServicePort(line.split("HiddenServicePort")[1].trim());
                            }
                        }

                        configs.add(config);
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return configs;
    }
}
