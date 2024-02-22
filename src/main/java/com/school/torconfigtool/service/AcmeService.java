package com.school.torconfigtool.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class AcmeService {

    private final CommandService commandService;

    private static final Logger logger = LoggerFactory.getLogger(AcmeService.class);

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
        String command = "/home/matys/.acme.sh/acme.sh --issue -d " + webTunnelUrl + " -w " + programLocation + "/onion/www/service-80/ --nginx --server letsencrypt_test --force";
        System.out.println("Generating certificate: " + command);

        Process certProcess = commandService.executeCommand(command);
        if (certProcess == null || certProcess.exitValue() != 0) {
            throw new Exception("Failed to generate certificate");
        }
    }

    /**
     * This method is used to install a certificate.
     * It constructs a command to install the certificate and then executes it.
     * If the command execution fails, it logs the error.
     *
     * @param webTunnelUrl The URL of the web tunnel where the certificate will be installed.
     */
    public void installCert(String webTunnelUrl) {
        // Get the current working directory
        String programLocation = System.getProperty("user.dir");

        // Construct the command to install the certificate
        String command = "/home/matys/.acme.sh/acme.sh --install-cert -d " + webTunnelUrl + " -d " + webTunnelUrl +
                " --key-file " + programLocation + "/onion/certs/service-80/key.pem" +
                " --fullchain-file " + programLocation + "/onion/certs/service-80/fullchain.pem" +
                " --reloadcmd";

        // Print the command to the console
        System.out.println(command);

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
                logger.error("Error during certificate installation. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            // Log any exceptions that occur during the process
            logger.error("Error during certificate installation", e);
        }
    }
}
