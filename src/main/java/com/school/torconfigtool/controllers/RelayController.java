package com.school.torconfigtool.controllers;

import com.school.torconfigtool.models.RelayInfo;
import com.school.torconfigtool.models.BridgeRelayConfig;
import com.school.torconfigtool.service.RelayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class RelayController {

    private final RelayService relayService;

    @Autowired
    public RelayController(RelayService relayService) {
        this.relayService = relayService;
    }

    @GetMapping("/relay-info")
    public List<RelayInfo> getRelayInfo() {
        List<RelayInfo> relayInfoList = new ArrayList<>();

        // Fetch the list of all bridges
        List<BridgeRelayConfig> bridges = relayService.getAllBridges();
        for (BridgeRelayConfig bridge : bridges) {
            RelayInfo relayInfo = new RelayInfo(Integer.parseInt(bridge.getControlPort()), bridge.getNickname(), "bridge");
            relayInfoList.add(relayInfo);
        }

        // Fetch the list of all guards
        List<BridgeRelayConfig> guards = relayService.getAllGuards();
        for (BridgeRelayConfig guard : guards) {
            RelayInfo relayInfo = new RelayInfo(Integer.parseInt(guard.getControlPort()), guard.getNickname(), "guard");
            relayInfoList.add(relayInfo);
        }

        return relayInfoList;
    }
}