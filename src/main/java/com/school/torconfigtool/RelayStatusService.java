package com.school.torconfigtool;

import com.school.torconfigtool.service.NginxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class RelayStatusService {

    private final TorrcService torrcService;
    private final NginxService nginxService;
    private static final Logger logger = LoggerFactory.getLogger(RelayStatusService.class);

    public RelayStatusService(TorrcService torrcService, NginxService nginxService) {
        this.torrcService = torrcService;
        this.nginxService = nginxService;
    }

    public String getRelayStatus(String relayNickname, String relayType) {
        String torrcFilePath = torrcService.buildTorrcFilePath(relayNickname, relayType).toString();
        int pid = getTorRelayPID(torrcFilePath);
        return pid > 0 ? "online" : (pid == -1 ? "offline" : "error");
    }

    public int getTorRelayPID(String torrcFilePath) {
        String relayNickname = new File(torrcFilePath).getName();
        String command = String.format("ps aux | grep -P '\\b%s\\b' | grep -v grep | awk '{print $2}'", relayNickname);

        // Log the command to be executed
        logger.debug("Command to execute: {}", command);

        try {
            List<String> outputLines = getCommandOutput(command);

            // Log the full output
            logger.debug("Command output: {}", outputLines);

            // Assuming the PID is on the first line, if not you need to check the outputLines list.
            if (!outputLines.isEmpty()) {
                String pidString = outputLines.getFirst();
                logger.debug("PID string: {}", pidString);
                return Integer.parseInt(pidString);
            } else {
                logger.debug("No PID found. Output was empty.");
                return -1;
            }
        } catch (IOException e) {
            logger.error("Error executing command to get PID: {}", command, e);
            return -1;
        }
    }

    private static List<String> getCommandOutput(String command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        Process process = processBuilder.start();

        // Read the entire output to ensure we're not missing anything.
        List<String> outputLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputLines.add(line);
            }
        }
        return outputLines;
    }

    public void waitForStatusChange(String relayNickname, String relayType, String expectedStatus) throws InterruptedException {
        System.out.println("Waiting for status change");
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 30000) { // 30 seconds timeout
            String status = getRelayStatus(relayNickname, relayType);
            System.out.println("Status: " + status);
            if (expectedStatus.equals(status)) {
                checkAndManageNginxStatus();
                break;
            }
            Thread.sleep(500); // wait for 500 milliseconds before the next check
        }
    }

    public void checkAndManageNginxStatus() {
        // Get the list of all webTunnels and Onion services
        List<String> allServices = nginxService.getAllServices();

        // Iterate over the list and check the status of each service
        for (String service : allServices) {
            String status = getRelayStatus(service, "onion");
            // If at least one service is online, start the Nginx service and return
            if ("online".equals(status)) {
                nginxService.startNginx();
                return;
            }
        }

        // If no service is online, stop the Nginx service
        nginxService.stopNginx();
    }

}
