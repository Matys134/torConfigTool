package com.school.torconfigtool.config;

import java.io.IOException;

public class ProxyStarter {

    public boolean start(String filePath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "sudo tor -f " + filePath);
        Process process = processBuilder.start();
        try {
            int exitCode = process.waitFor();
            return exitCode == 0;
        } finally {
            process.destroy();
        }
    }
}