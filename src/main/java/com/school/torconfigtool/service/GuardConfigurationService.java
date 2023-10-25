package com.school.torconfigtool.service;

import org.springframework.stereotype.Service;

@Service
public class GuardConfigurationService {
    public boolean updateGuardConfiguration(String nickname, String orPort, String contact) {
        try {
            String scriptPath = "shellScripts/configure-relay.sh";  // Update this path

            ProcessBuilder processBuilder = new ProcessBuilder("bash", scriptPath,
                    nickname, "", orPort, contact);

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            return exitCode == 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
