package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * This class is a REST controller for updating the guard configuration.
 * It uses the GuardConfigurationService to perform the update operation.
 */
@RestController
@RequestMapping("/update-guard-config")
public class GuardConfigurationController {
    private static final Logger logger = LoggerFactory.getLogger(GuardConfigurationController.class);
    private final GuardConfigurationService guardConfigurationService;

    /**
     * Constructor for the GuardConfigurationController.
     * @param guardConfigurationService The service used for updating the guard configuration.
     */
    public GuardConfigurationController(GuardConfigurationService guardConfigurationService) {
        this.guardConfigurationService = guardConfigurationService;
    }

    /**
     * This method is a POST endpoint for updating the guard configuration.
     * @param config The new guard relay configuration.
     * @return A ResponseEntity indicating the result of the update operation.
     */
    @PostMapping
    public ResponseEntity<?> updateGuardConfiguration(@RequestBody GuardRelayConfig config) {
        try {
            boolean success = guardConfigurationService.updateConfiguration(config);
            if (success) {
                return handleUpdateSuccess(config);
            } else {
                return handleUpdateFailure(config);
            }
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * This method handles the case when the update operation is successful.
     * @param config The guard relay configuration that was updated.
     * @return A ResponseEntity with a success message.
     */
    private ResponseEntity<?> handleUpdateSuccess(GuardRelayConfig config) {
        logger.info("Guard configuration updated successfully for relay: {}", config.getNickname());
        return ResponseEntity.ok(Map.of("success", "Guard configuration updated successfully"));
    }

    /**
     * This method handles the case when the update operation fails.
     * @param config The guard relay configuration that failed to update.
     * @return A ResponseEntity with an error message.
     */
    private ResponseEntity<?> handleUpdateFailure(GuardRelayConfig config) {
        logger.warn("Failed to update guard configuration for relay: {}", config.getNickname());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to update guard configuration"));
    }

    /**
     * This method handles the case when an exception occurs during the update operation.
     * @param e The exception that occurred.
     * @return A ResponseEntity with an error message.
     */
    private ResponseEntity<?> handleException(Exception e) {
        logger.error("Exception occurred while updating guard configuration", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred"));
    }

    /**
     * This method is a GET endpoint for checking the availability of ports for a given relay.
     * @param nickname The nickname of the relay.
     * @param orPort The OR port of the relay.
     * @param controlPort The control port of the relay.
     * @return A ResponseEntity indicating whether the ports are available.
     */
    @GetMapping("/check-port-availability")
    public ResponseEntity<?> checkPortAvailability(@RequestParam String nickname, @RequestParam int orPort, @RequestParam int controlPort) {
        boolean arePortsAvailable = RelayUtils.portsAreAvailable(nickname, orPort, controlPort);

        return ResponseEntity.ok(Map.of("available", arePortsAvailable));
    }
}