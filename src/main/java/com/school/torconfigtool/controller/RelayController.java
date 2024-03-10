package com.school.torconfigtool.controller;

import com.school.torconfigtool.model.RelayInfo;
import com.school.torconfigtool.service.RelayInformationService;
import com.school.torconfigtool.model.BridgeConfig;
import com.school.torconfigtool.model.GuardConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a REST controller that handles requests related to relay information.
 * It uses Spring's @RestController annotation to indicate that it's a controller and
 * @RequestMapping to map the requests to "/api".
 */
@RestController
@RequestMapping("/api")
public class RelayController {

    // RelayService instance used to fetch relay information
    private final RelayInformationService relayInformationService;

    /**
     * Constructor for the RelayController class.
     * It uses Spring's @Autowired annotation to automatically inject a RelayService instance.
     *
     * @param relayInformationService the RelayService instance to be used
     */
    @Autowired
    public RelayController(RelayInformationService relayInformationService) {
        this.relayInformationService = relayInformationService;
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
            RelayInfo relayInfo = new RelayInfo(Integer.parseInt(bridge.getControlPort()), bridge.getNickname(), "bridge");
            relayInfoList.add(relayInfo);
        }

        // Fetch the list of all guards
        List<GuardConfig> guards = relayInformationService.getAllGuards();
        for (GuardConfig guard : guards) {
            RelayInfo relayInfo = new RelayInfo(Integer.parseInt(guard.getControlPort()), guard.getNickname(), "guard");
            relayInfoList.add(relayInfo);
        }

        return relayInfoList;
    }
}