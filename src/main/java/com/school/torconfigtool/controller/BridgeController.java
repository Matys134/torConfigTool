package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for the bridge configuration.
 */
@Controller
@RequestMapping("/bridge")
public class BridgeController {

    private final BridgeService bridgeService;

    /**
     * Constructor for the BridgeController.
     * @param bridgeService The bridge service.
     */
    @Autowired
    public BridgeController(BridgeService bridgeService) {
        this.bridgeService = bridgeService;
    }

    /**
     * Endpoint for getting the bridge configuration form.
     * @return The name of the setup view.
     */
    @GetMapping
    public String getBridgeConfigurationForm() {
        return "setup";
    }

    /**
     * Endpoint for configuring the bridge.
     * @param bridgeType The type of bridge.
     * @param bridgePort The port of the bridge.
     * @param bridgeTransportListenAddr The transport listen to address of the bridge.
     * @param bridgeContact The contact of the bridge.
     * @param bridgeNickname The nickname of the bridge.
     * @param webtunnelDomain The domain of the web tunnel.
     * @param bridgeControlPort The control port of the bridge.
     * @param webtunnelUrl The URL of the web tunnel.
     * @param webtunnelPort The port of the web tunnel.
     * @param bridgeBandwidth The bandwidth of the bridge.
     * @param model The model for the view.
     * @return The name of the setup view.
     */
    @PostMapping("/configure")
    public String configureBridge(@RequestParam String bridgeType,
                                  @RequestParam(required = false) Integer bridgePort,
                                  @RequestParam(required = false) Integer bridgeTransportListenAddr,
                                  @RequestParam String bridgeContact,
                                  @RequestParam String bridgeNickname,
                                  @RequestParam(required = false) String webtunnelDomain,
                                  @RequestParam int bridgeControlPort,
                                  @RequestParam(required = false) String webtunnelUrl,
                                  @RequestParam(required = false) Integer webtunnelPort,
                                  @RequestParam(required = false) Integer bridgeBandwidth,
                                  Model model) {
        try {
            bridgeService.configureBridge(bridgeType, bridgePort, bridgeTransportListenAddr, bridgeContact, bridgeNickname, webtunnelDomain, bridgeControlPort, webtunnelUrl, webtunnelPort, bridgeBandwidth);
            model.addAttribute("successMessage", "Tor Bridge configured successfully!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to configure Tor Bridge: " + e.getMessage());
        }
        return "setup";
    }
}