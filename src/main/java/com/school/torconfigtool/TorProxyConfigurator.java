package com.school.torconfigtool;

import java.io.*;

public class TorProxyConfigurator {

    public static boolean configureTorProxy() {
        try {
            // Check if the torrc file for the proxy exists, create it if not
            File torrcFile = new File("torrc/local-torrc-proxy");
            if (!torrcFile.exists()) {
                torrcFile.createNewFile();
            }

            // Write the Tor Proxy configuration to the torrc file
            FileWriter fw = new FileWriter(torrcFile);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write("SocksPort 192.168.1.100:9050");
            bw.newLine();
            bw.write("SocksPolicy accept 192.168.1.0/24");
            bw.newLine();
            bw.write("RunAsDaemon 1");
            bw.newLine();
            bw.write("DataDirectory /var/lib/tor");

            bw.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean startTorProxy() {
        try {
            // Execute a command to start the Tor Proxy service
            Process process = Runtime.getRuntime().exec("sudo systemctl start tor@default");

            // Wait for the process to complete
            int exitCode = process.waitFor();

            // Check the exit code to determine if the start was successful
            return exitCode == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Add methods to stop and check the status of the Tor Proxy if needed
}
