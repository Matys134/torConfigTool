package com.school.torconfigtool.service;

import com.school.torconfigtool.model.BridgeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Service class for managing web tunnels.
 */
@Service
public class WebtunnelService {
    private static final String TORRC_DIRECTORY_PATH = "torrc";
    private static final String TORRC_FILE_PREFIX = "torrc-";
    private static final Logger logger = LoggerFactory.getLogger(WebtunnelService.class);


    /**
     * Sets up a web tunnel.
     *
     * @param webTunnelUrl The URL of the web tunnel.
     * @throws Exception If an error occurs during setup.
     */
    public void setupWebtunnel(String webTunnelUrl) throws Exception {

        // Check if Nginx is running, if not, start it


        // Get the current program location
        String programLocation = System.getProperty("user.dir");

        // Change the ownership of the directory
        String chownCommand = "sudo chown -R matys:matys " + programLocation + "/onion/www/service-80";
        Process chownProcess = executeCommand(chownCommand);
        if (chownProcess == null || chownProcess.exitValue() != 0) {
            throw new Exception("Failed to change ownership of the directory");
        }

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

        Process certProcess = executeCommand(command);
        if (certProcess == null || certProcess.exitValue() != 0) {
            throw new Exception("Failed to generate certificate");
        }
    }

    /**
     * Executes a command.
     *
     * @param command The command to execute.
     * @return The process of the executed command.
     */
    private Process executeCommand(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("Error during command execution. Exit code: " + exitCode);
            }
            return process;
        } catch (IOException | InterruptedException e) {
            logger.error("Error during command execution", e);
            return null;
        }
    }

    /**
     * Updates the torrc file with the given BridgeConfig.
     *
     * @param config The BridgeConfig to use.
     * @throws IOException If an error occurs during file operations.
     */
    public void updateTorrcFile(BridgeConfig config) throws IOException {
        String torrcFileName = TORRC_FILE_PREFIX + config.getNickname() + "_bridge";
        Path torrcFilePath = Paths.get(TORRC_DIRECTORY_PATH, torrcFileName).toAbsolutePath().normalize();

        // Read all lines from the file
        List<String> lines = Files.readAllLines(torrcFilePath);
        for (int i = 0; i < lines.size(); i++) {
            // Update the line that starts with "ServerTransportOptions webtunnel url"
            if (lines.get(i).startsWith("ServerTransportOptions webtunnel url")) {
                lines.set(i, "ServerTransportOptions webtunnel url=https://" + config.getWebtunnelUrl() + "/" + config.getPath());
                break;
            }
        }
        // Write the updated lines back to the file
        Files.write(torrcFilePath, lines);
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