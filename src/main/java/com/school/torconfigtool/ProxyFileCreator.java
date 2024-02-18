package com.school.torconfigtool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class is responsible for creating a proxy configuration file.
 */
public class ProxyFileCreator {

    /**
     * This method creates a new file at the specified file path.
     * If the file already exists, it simply returns the file.
     * If the file does not exist, it attempts to create a new file.
     * If the file creation fails, it throws an IOException.
     *
     * @param filePath The path where the file should be created.
     * @return The created file.
     * @throws IOException If the file creation fails.
     */
    public File createFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Failed to create " + filePath);
        }
        return file;
    }

    /**
     * This method writes a proxy configuration to the specified file.
     * The configuration includes the local IP address, the SocksPort, the SocksPolicy,
     * the RunAsDaemon setting, and the DNSPort.
     * If the writing fails, it throws an IOException.
     *
     * @param file The file where the configuration should be written.
     * @param localIpAddress The local IP address to be used in the configuration.
     * @throws IOException If the writing fails.
     */
    public void writeConfiguration(File file, String localIpAddress) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("SocksPort " + localIpAddress + ":9050");
            bw.newLine();
            bw.write("SocksPolicy accept 192.168.1.0/24");
            bw.newLine();
            bw.write("RunAsDaemon 1");
            bw.newLine();
            bw.write("DNSPort " + localIpAddress + ":53");
        }
    }
}