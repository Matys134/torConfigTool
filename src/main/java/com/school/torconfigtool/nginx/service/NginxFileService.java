package com.school.torconfigtool.nginx.service;

import com.school.torconfigtool.util.CommandExecutor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * Service class for handling Nginx file operations.
 */
@Service
public class NginxFileService {

    private final CommandExecutor commandExecutor;

    /**
     * Constructor for NginxFileService.
     *
     * @param commandExecutor the command executor to be used for executing commands
     */
    public NginxFileService(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    /**
     * Retrieves the index.html file from the service directory.
     *
     * @param currentDirectory the current directory path
     * @return the index.html file
     * @throws IOException if an I/O error occurs
     */
    public File getFile(String currentDirectory) throws IOException {
        File wwwDir = getWwwDir(currentDirectory);
        File serviceDir = getServiceDir(wwwDir);
        return new File(serviceDir, "index.html");
    }

    /**
     * Retrieves the www directory, creating it if it does not exist.
     *
     * @param currentDirectory the current directory path
     * @return the www directory
     * @throws IOException if an I/O error occurs
     */
    private File getWwwDir(String currentDirectory) throws IOException {
        File wwwDir = new File(currentDirectory + "/onion/www");
        createDirectory(wwwDir);
        return wwwDir;
    }

    /**
     * Retrieves the service directory, creating it if it does not exist.
     *
     * @param parentDir the parent directory
     * @return the service directory
     * @throws IOException if an I/O error occurs
     */
    private File getServiceDir(File parentDir) throws IOException {
        File serviceDir = new File(parentDir, "service-" + 80);
        createDirectory(serviceDir);
        return serviceDir;
    }

    /**
     * Creates a directory if it does not exist.
     *
     * @param dir the directory to be created
     * @throws IOException if an I/O error occurs
     */
    private void createDirectory(File dir) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create directory: " + dir.getAbsolutePath());
        }
    }

    /**
     * Installs a certificate.
     *
     * @param webTunnelUrl the web tunnel URL
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the current thread is interrupted
     */
    public void installCert(String webTunnelUrl) throws IOException, InterruptedException {
        String command = createCommand(webTunnelUrl);
        executeCommand(command);
    }

    /**
     * Creates a command for installing a certificate.
     *
     * @param webTunnelUrl the web tunnel URL
     * @return the command string
     */
    private String createCommand(String webTunnelUrl) {
        String programLocation = System.getProperty("user.dir");
        return "/home/matys/.acme.sh/acme.sh --install-cert -d " + webTunnelUrl + " -d " + webTunnelUrl +
                " --key-file " + programLocation + "/onion/certs/service-80/key.pem" +
                " --fullchain-file " + programLocation + "/onion/certs/service-80/fullchain.pem" +
                " --reloadcmd";
    }

    /**
     * Executes a command.
     *
     * @param command the command to be executed
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the current thread is interrupted
     */
    private void executeCommand(String command) throws IOException, InterruptedException {
        Process process = commandExecutor.executeCommand(command);
        handleProcessResult(process);
    }

    /**
     * Handles the result of a process.
     *
     * @param process the process to be handled
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the current thread is interrupted
     */
    private void handleProcessResult(Process process) throws IOException, InterruptedException {
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Error during certificate installation. Exit code: " + exitCode);
        }
    }
}