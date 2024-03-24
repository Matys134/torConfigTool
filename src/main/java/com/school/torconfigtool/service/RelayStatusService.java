package com.school.torconfigtool.service;

import lombok.Setter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * RelayStatusService is a service class responsible for managing and checking the status of Tor relays.
 */
@Service
public class RelayStatusService {

    private final TorFileService torFileService;
    @Setter
    private StatusChangeListener statusChangeListener;

    public RelayStatusService(TorFileService torFileService) {
        this.torFileService = torFileService;
    }

    /**
     * This method is used to get the status of a given relay.
     * It reads the PID of the relay process and returns its status.
     *
     * @param relayNickname The nickname of the relay.
     * @param relayType The type of the relay.
     * @return The status of the relay. Returns "online" if PID > 0, "offline" if PID == -1, "error" otherwise.
     */
    public String getRelayStatus(String relayNickname, String relayType) {
        String torrcFilePath = torFileService.buildTorrcFilePath(relayNickname, relayType).toString();
        int pid = getTorRelayPID(torrcFilePath);
        return pid > 0 ? "online" : (pid == -1 ? "offline" : "error");
    }

    /**
     * This method is used to get the PID of a given Tor relay.
     * It executes a bash command to get the PID and returns it.
     *
     * @param torrcFilePath The path to the torrc file of the relay.
     * @return The PID of the relay. Returns -1 if no PID found or an error occurs.
     */
    public int getTorRelayPID(String torrcFilePath) {
        String relayNickname = new File(torrcFilePath).getName();
        String command = String.format("ps aux | grep -P '\\b%s\\b' | grep -v grep | awk '{print $2}'", relayNickname);

        try {
            List<String> outputLines = CommandService.getCommandOutput(command);

            // Assuming the PID is on the first line, if not, you need to check the outputLines list.
            if (!outputLines.isEmpty()) {
                String pidString = outputLines.getFirst();
                return Integer.parseInt(pidString);
            } else {
                return -1;
            }
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * This method waits for a status change of a given relay to a specified status.
     * It checks the status every 500 milliseconds and stops after 30 seconds or when the expected status is reached.
     * If the expected status is reached, it also checks and manages the Nginx status.
     *
     * @param relayNickname The nickname of the relay.
     * @param relayType The type of the relay.
     * @param expectedStatus The expected status of the relay.
     * @throws InterruptedException If the thread is interrupted while waiting.
     */
    public void waitForStatusChange(String relayNickname, String relayType, String expectedStatus) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        Runnable statusCheck = () -> {
            if (System.currentTimeMillis() - startTime >= 30000) { // 30-second timeout
                executor.shutdown();
            } else {
                String status = getRelayStatus(relayNickname, relayType);
                if (expectedStatus.equals(status)) {
                    if (statusChangeListener != null) {
                        statusChangeListener.onStatusChange(status);
                    }
                    executor.shutdown();
                }
            }
        };

        executor.scheduleAtFixedRate(statusCheck, 0, 500, TimeUnit.MILLISECONDS);
    }

}