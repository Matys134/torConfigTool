package com.school.torconfigtool.util;

import com.school.torconfigtool.RelayUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * This is a Rest Controller class for managing ports.
 * It is mapped to the "/port" endpoint.
 */
@RestController
@RequestMapping("/port")
public class PortController {

    /**
     * This method checks the availability of the given ports.
     * It is mapped to the "/check-availability" endpoint and responds to GET requests.
     *
     * @param nickname    The nickname of the relay.
     * @param orPort      The OR port number to check.
     * @param controlPort The control port number to check.
     * @return ResponseEntity<?> A response entity containing a map with a single key-value pair.
     *                           The key is "available" and the value is a boolean indicating whether the ports are available.
     */
    @GetMapping("/check-availability")
    public ResponseEntity<?> checkPortAvailability(@RequestParam String nickname, @RequestParam int orPort, @RequestParam int controlPort) {
        boolean arePortsAvailable = RelayUtils.portsAreAvailable(nickname, orPort, controlPort);

        return ResponseEntity.ok(Map.of("available", arePortsAvailable));
    }
}