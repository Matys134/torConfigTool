package com.school.torconfigtool;

import org.springframework.stereotype.Service;

@Service
public class GuardConfigurationService {
    public boolean updateGuardConfiguration(String nickname, String orPort, String contact) {
        try {
            // Execute your shell script with the provided parameters
            String scriptCommand = "/path/to/your/script.sh " + nickname + " " + orPort + " " + contact;
            Process process = Runtime.getRuntime().exec(scriptCommand);

            // Wait for the process to complete
            int exitCode = process.waitFor();

            // Check the exit code to determine if the update was successful
            return exitCode == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
