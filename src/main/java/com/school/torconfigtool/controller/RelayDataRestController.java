package com.school.torconfigtool.controller;

import com.school.torconfigtool.model.BridgeConfig;
import com.school.torconfigtool.model.GuardConfig;
import com.school.torconfigtool.model.RelayInfo;
import com.school.torconfigtool.service.RelayDataService;
import com.school.torconfigtool.model.RelayData;
import com.school.torconfigtool.service.RelayInformationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a REST controller that handles requests related to relay data and events.
 */
@RestController
@RequestMapping("/relay-data")
public class RelayDataRestController {

    // Maps to store relay data and events, with relay ID as the key
    private final Map<Integer, Deque<RelayData>> relayDataMap = new ConcurrentHashMap<>();
    private final Map<Integer, Deque<String>> relayEventMap = new ConcurrentHashMap<>();

    // Service to handle operations related to relay data and events
    private final RelayDataService relayDataService;
    private final RelayInformationService relayInformationService;

    /**
     * Constructor for the DataController class.
     * @param relayDataService The service to handle operations related to relay data and events.
     */
    public RelayDataRestController(RelayDataService relayDataService, RelayInformationService relayInformationService) {
        this.relayDataService = relayDataService;
        this.relayInformationService = relayInformationService;
    }


    @PostMapping("/relays/{relayId}")
    public ResponseEntity<String> createRelayData(@PathVariable int relayId, @RequestBody RelayData relayData) {
        relayDataService.handleRelayData(relayId, relayData, relayDataMap);
        return ResponseEntity.ok("Data received successfully for Relay ID: " + relayId);
    }


    @PostMapping("/relays/{relayId}/event")
    public ResponseEntity<String> createRelayEvent(@PathVariable int relayId, @RequestBody Map<String,
            String> eventData) {
        relayDataService.handleRelayEvent(relayId, eventData, relayDataMap, relayEventMap);
        return ResponseEntity.ok("Event received successfully for Relay ID: " + relayId);
    }

    /**
     * Endpoint to get relay events.
     * @param relayId The ID of the relay.
     * @return A list of events for the given relay ID.
     */
    @GetMapping("/relays/{relayId}/events")
    public List<String> getRelayEvents(@PathVariable int relayId) {
        return new ArrayList<>(relayEventMap.getOrDefault(relayId, new LinkedList<>()));
    }

    /**
     * Endpoint to get relay data.
     * @param relayId The ID of the relay.
     * @return A list of data for the given relay ID.
     */
    @GetMapping("/relays/{relayId}")
    public List<RelayData> getRelayData(@PathVariable int relayId) {
        synchronized (relayDataMap) {
            return new ArrayList<>(relayDataMap.getOrDefault(relayId, new LinkedList<>()));
        }
    }

    /**
     * Endpoint to get control ports.
     * @return A list of control ports.
     */
    @GetMapping("/control-ports")
    public List<Integer> getControlPorts() {
        return new ArrayList<>(relayDataMap.keySet());
    }

    /**
     * This method handles GET requests to "/relay-info".
     * It fetches and returns a list of all relay information.
     *
     * @return a list of RelayInfo instances
     */
    @GetMapping("/relay-info")
    public List<RelayInfo> getRelayInfo() {
        List<RelayInfo> relayInfoList = new ArrayList<>();

        // Fetch the list of all bridges
        List<BridgeConfig> bridges = relayInformationService.getAllBridges();
        for (BridgeConfig bridge : bridges) {
            RelayInfo relayInfo = new RelayInfo(Integer.parseInt(bridge.getControlPort()), bridge.getNickname(),
                    "bridge");
            relayInfoList.add(relayInfo);
        }

        // Fetch the list of all guards
        List<GuardConfig> guards = relayInformationService.getAllGuards();
        for (GuardConfig guard : guards) {
            RelayInfo relayInfo = new RelayInfo(Integer.parseInt(guard.getControlPort()), guard.getNickname(),
                    "guard");
            relayInfoList.add(relayInfo);
        }

        return relayInfoList;
    }
}