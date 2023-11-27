package com.school.torconfigtool.config;

import java.io.*;

public class ProxyConfigurator {

    public static boolean configureProxy() {
        try {
            // Check if the torrc file for the proxy exists, create it if not
            File torrcFile = new File("torrc/proxy/torrc-proxy");
            if (!torrcFile.exists()) {
                torrcFile.createNewFile();
            }

            // Write the Tor Proxy configuration to the torrc file
            FileWriter fw = new FileWriter(torrcFile);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write("SocksPort 192.168.2.119:9050");
            bw.newLine();
            bw.write("SocksPolicy accept 192.168.1.0/24");
            bw.newLine();
            bw.write("RunAsDaemon 1");
            bw.newLine();
            bw.write("DNSPort 192.168.2.119:53");

            bw.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean startProxy() {
        try {
            // Start the Tor Proxy
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "sudo tor -f /home/matys/IdeaProjects/torConfigTool/torrc/proxy/torrc-proxy");
            // print command for debugging
            System.out.println(processBuilder.command());
            Process process = processBuilder.start();


            try {
                int exitCode = process.waitFor();

                // Log the exit code
                System.out.println("Command exit code: " + exitCode);

                return exitCode == 0;
            } finally {
                process.destroy();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Add methods to stop and check the status of the Tor Proxy if needed
}
