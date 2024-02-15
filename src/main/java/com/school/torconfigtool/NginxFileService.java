package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class NginxFileService {

    private static final Logger logger = LoggerFactory.getLogger(NginxFileService.class);
    private final CommandExecutor commandExecutor;

    public NginxFileService(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public File getFile(String currentDirectory) throws IOException {
        File wwwDir = new File(currentDirectory + "/onion/www");
        if (!wwwDir.exists() && !wwwDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + wwwDir.getAbsolutePath());
        }

        File serviceDir = new File(wwwDir, "service-" + 80);
        if (!serviceDir.exists() && !serviceDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + serviceDir.getAbsolutePath());
        }

        return new File(serviceDir, "index.html");
    }

    public void installCert(String webTunnelUrl) throws IOException, InterruptedException {
        String command = createCommand(webTunnelUrl);
        executeCommand(command);
    }

    private String createCommand(String webTunnelUrl) {
        String programLocation = System.getProperty("user.dir");
        return "/home/matys/.acme.sh/acme.sh --install-cert -d " + webTunnelUrl + " -d " + webTunnelUrl +
                " --key-file " + programLocation + "/onion/certs/service-80/key.pem" +
                " --fullchain-file " + programLocation + "/onion/certs/service-80/fullchain.pem" +
                " --reloadcmd";
    }

    private void executeCommand(String command) throws IOException, InterruptedException {
        Process process = commandExecutor.executeCommand(command);
        handleProcessResult(process);
    }

    private void handleProcessResult(Process process) throws IOException, InterruptedException {
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Error during certificate installation. Exit code: " + exitCode);
        }
    }
}