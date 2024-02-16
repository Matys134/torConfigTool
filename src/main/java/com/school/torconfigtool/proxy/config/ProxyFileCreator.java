package com.school.torconfigtool.proxy.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class is responsible for creating and writing configuration to a proxy file.
 */
public class ProxyFileCreator {

    /**
     * Creates a new file at the specified file path.
     *
     * @param filePath The path where the file should be created.
     * @return The created file.
     * @throws IOException If the file cannot be created.
     */
    public File createFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Failed to create " + filePath);
        }
        return file;
    }

    /**
     * Writes the configuration to the provided file.
     *
     * @param file The file where the configuration should be written.
     * @param localIpAddress The local IP address to be used in the configuration.
     * @throws IOException If the configuration cannot be written to the file.
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