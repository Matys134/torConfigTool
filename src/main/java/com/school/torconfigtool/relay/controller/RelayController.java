package com.school.torconfigtool.relay.controller;

import com.school.torconfigtool.relay.config.RelayInfo;
import com.school.torconfigtool.RelayService;
import com.school.torconfigtool.bridge.config.BridgeRelayConfig;
import com.school.torconfigtool.guard.config.GuardRelayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * The RelayController class is responsible for handling HTTP requests related to relay information.
 * It uses the RelayService to fetch the required data.
 */
@RestController
@RequestMapping("/api")
public class RelayController {

    private final RelayService relayService;

    /**
     * Constructs a new RelayController with the specified RelayService.
     * @param relayService the RelayService to be used by the RelayController
     */
    @Autowired
    public RelayController(RelayService relayService) {
        this.relayService = relayService;
    }

    /**
     * Handles the GET request to fetch information about all bridge relays.
     * @return a list of RelayInfo objects representing the bridge relays
     */
    @GetMapping("/relay-info/bridges")
    public List<RelayInfo> getBridgeRelayInfo() {
        List<RelayInfo> relayInfoList = new ArrayList<>();

        // Fetch the list of all bridges
        List<BridgeRelayConfig> bridges = relayService.getAllBridges();
        for (BridgeRelayConfig bridge : bridges) {
            RelayInfo relayInfo = new RelayInfo(Integer.parseInt(bridge.getControlPort()), bridge.getNickname(), "bridge");
            relayInfoList.add(relayInfo);
        }

        return relayInfoList;
    }

    /**
     * Handles the GET request to fetch information about all guard relays.
     * @return a list of RelayInfo objects representing the guard relays
     */
    @GetMapping("/relay-info/guards")
    public List<RelayInfo> getGuardRelayInfo() {
        List<RelayInfo> relayInfoList = new ArrayList<>();

        // Fetch the list of all guards
        List<GuardRelayConfig> guards = relayService.getAllGuards();
        for (GuardRelayConfig guard : guards) {
            RelayInfo relayInfo = new RelayInfo(Integer.parseInt(guard.getControlPort()), guard.getNickname(), "guard");
            relayInfoList.add(relayInfo);
        }

        return relayInfoList;
    }
}