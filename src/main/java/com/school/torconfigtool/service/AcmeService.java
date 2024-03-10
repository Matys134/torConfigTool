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
        System.out.println("Generating certificate: " + command);

        Process certProcess = commandService.executeCommand(command);
        if (certProcess == null || certProcess.exitValue() != 0) {
            throw new Exception("Failed to generate certificate");
        }
    }

    /**
     * This method is used to install a certificate.
     * It constructs a command to install the certificate and then executes it.
     * If the command execution fails, it throws an exception.
     *
     * @param webTunnelUrl The URL of the web tunnel where the certificate will be installed.
     */
    public void installCert(String webTunnelUrl) throws IOException, InterruptedException {
        // Get the current working directory
        String programLocation = System.getProperty("user.dir");

        // Construct the command to install the certificate
        String username = System.getProperty("user.name");
        Process process = createCertificateInstallationProcess(webTunnelUrl, username, programLocation);
        int exitCode = process.waitFor();

        // If the exit code is not 0, throw an exception
        if (exitCode != 0) {
            throw new IOException("Error during certificate installation. Exit code: " + exitCode);
        }
    }

    private static Process createCertificateInstallationProcess(String webTunnelUrl, String username, String programLocation) throws IOException {
        String command = "/home/" + username + "/.acme.sh/acme.sh --install-cert -d " + webTunnelUrl + " -d " + webTunnelUrl +
                " --key-file " + programLocation + "/onion/certs/service-80/key.pem" +
                " --fullchain-file " + programLocation + "/onion/certs/service-80/fullchain.pem" +
                " --reloadcmd";

        // Create a new process builder
        ProcessBuilder processBuilder = new ProcessBuilder();

        // Set the command for the process builder
        processBuilder.command("bash", "-c", command);

        // Start the process and wait for it to finish
        return processBuilder.start();
    }
}
