package com.school.torconfigtool.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProxyStarter {

    public long start(String filePath) throws IOException, InterruptedException {
        long pid = getRunningTorProcessId();
        if (pid != -1) {
            return pid;
        }

        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "sudo tor -f " + filePath);
        Process process = processBuilder.start();
        try {
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return process.pid();
            }
        } finally {
            process.destroy();
        }
        return -1;
    }

    public boolean stop(long pid) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "sudo kill " + pid);
        Process process = processBuilder.start();
        try {
            int exitCode = process.waitFor();
            return exitCode == 0;
        } finally {
            process.destroy();
        }
    }

    public long getRunningTorProcessId() throws IOException { // Change this line
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "pgrep tor");
        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = reader.readLine();
            if (line != null) {
                return Long.parseLong(line);
            }
        }
        return -1;
    }
}