package com.school.torconfigtool.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class AcmeService {

    private final CommandService commandService;

    public AcmeService(CommandService commandService) {
        this.commandService = commandService;
    }

    public void generateCertificate(String webTunnelUrl, String programLocation) throws Exception {
        // Create the directory for the certificate files
        String certDirectory = programLocation + "/onion/certs/service-80/";
        File dir = new File(certDirectory);
        boolean isDirectoryCreated = dir.mkdirs();
        if (!isDirectoryCreated && !dir.exists()) {
            throw new IOException("Failed to create directory " + certDirectory);
        }

        // Generate the certificate
        String username = System.getProperty("user.name");
        String command = "/home/" + username + "/.acme.sh/acme.sh --issue -d " + webTunnelUrl + " -w " + programLocation + "/onion/www/service-80/ --nginx --server letsencrypt --force";

        Process certProcess = commandService.executeCommand(command);
        if (certProcess == null || certProcess.exitValue() != 0) {
            throw new Exception("Failed to generate certificate");
        }
    }

    /**
     * Installs the certificate for the given web tunnel URL.
     *
     * @param webTunnelUrl The web tunnel URL.
     * @throws Exception If an error occurs during the installation.
     */
    public void installCert(String webTunnelUrl) throws Exception {
        // Get the current working directory
        String programLocation = System.getProperty("user.dir");

        // Construct the command to install the certificate
        String username = System.getProperty("user.name");
        String command = "/home/" + username + "/.acme.sh/acme.sh --install-cert -d " + webTunnelUrl + " -d " + webTunnelUrl +
                " --key-file " + programLocation + "/onion/certs/service-80/key.pem" +
                " --fullchain-file " + programLocation + "/onion/certs/service-80/fullchain.pem" +
                " --reloadcmd";

        // Create a new process builder
        ProcessBuilder processBuilder = new ProcessBuilder();

        // Set the command for the process builder
        processBuilder.command("bash", "-c", command);

        try {
            // Start the process and wait for it to finish
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            // If the exit code is not 0, log an error
            if (exitCode != 0) {
                throw new Exception("Error during certificate installation. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            // Create a custom error message
            String errorMessage = "Error during certificate installation: " + e.getMessage();
            // Throw a new exception with the custom error message
            throw new Exception(errorMessage, e);
        }
    }
}