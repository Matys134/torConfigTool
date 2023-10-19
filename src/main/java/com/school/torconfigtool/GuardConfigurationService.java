package com.school.torconfigtool;

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

            if (exitCode == 0) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
