package com.school.torconfigtool;

import com.school.torconfigtool.service.NginxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a Spring Boot controller for managing Tor bridges.
 * It provides endpoints for configuring, running, and managing Tor bridges.
 */
@Controller
@RequestMapping("/bridge")
public class BridgeController {

    private final RelayService relayService;
    private final NginxService nginxService;
    private final BridgeService bridgeService;
    private final SnowflakeProxyService snowflakeProxyService;

    /**
     * Constructor for the BridgeController.
     * @param relayService The relay service.
     * @param nginxService The Nginx service.
     * @param bridgeService The bridge service.
     * @param snowflakeProxyService The snowflake proxy service.
     */
    @Autowired
    public BridgeController(RelayService relayService, NginxService nginxService, BridgeService bridgeService, SnowflakeProxyService snowflakeProxyService) {
        this.relayService = relayService;
        this.nginxService = nginxService;
        this.bridgeService = bridgeService;
        this.snowflakeProxyService = snowflakeProxyService;
    }

    /**
     * Endpoint for getting the bridge configuration form.
     * @return The name of the setup view.
     */
    @GetMapping
    public String bridgeConfigurationForm() {
        return "setup";
    }


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
                                  @RequestParam(defaultValue = "false") boolean startBridgeAfterConfig,
                                  @RequestParam(required = false) Integer bridgeBandwidth,
                                  Model model) {
        bridgeService.configureBridge(bridgeType, bridgePort, bridgeTransportListenAddr, bridgeContact, bridgeNickname, webtunnelDomain, bridgeControlPort, webtunnelUrl, webtunnelPort, startBridgeAfterConfig, bridgeBandwidth, model);
        return "setup";
    }

    /**
     * This method is responsible for running the Snowflake proxy.
     * It creates a new instance of BridgeRelayConfig and calls the runSnowflakeProxy method on it.
     * If the proxy starts successfully, it returns a response entity with a success message and HTTP status OK.
     * If an exception occurs during the process, it returns a response entity with an error message and HTTP status INTERNAL_SERVER_ERROR.
     *
     * @return ResponseEntity<String> - The response entity containing the result of the operation and the corresponding HTTP status.
     */
    @PostMapping("/run-snowflake-proxy")
    public ResponseEntity<String> runSnowflakeProxy() {
        try {
            snowflakeProxyService.runSnowflakeProxy();
            return new ResponseEntity<>("Snowflake proxy started successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error starting snowflake proxy: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This method is responsible for checking if the bridge limit has been reached.
     * It returns a response entity with a map containing the bridge limit reached status and the bridge count for the given bridge type.
     *
     * @param bridgeType - The type of bridge to check the limit for.
     * @return ResponseEntity<Map<String, Object>> - The response entity containing the map with the bridge limit reached status and the bridge count.
     */
    @GetMapping("/limit-reached")
    public ResponseEntity<Map<String, Object>> checkBridgeLimit(@RequestParam String bridgeType) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Integer> bridgeCountByType = relayService.getBridgeCountByType();

        if (!RelayService.isLimitOn()) {
            response.put("bridgeLimitReached", false);
            response.put("bridgeCount", bridgeCountByType.get(bridgeType));
            return ResponseEntity.ok(response);
        }

        switch (bridgeType) {
            case "obfs4":
                response.put("bridgeLimitReached", bridgeCountByType.get("obfs4") >= 2);
                response.put("bridgeCount", bridgeCountByType.get("obfs4"));
                break;
            case "webtunnel":
                response.put("bridgeLimitReached", bridgeCountByType.get("webtunnel") >= 1);
                response.put("bridgeCount", bridgeCountByType.get("webtunnel"));
                break;
            case "snowflake":
                response.put("bridgeLimitReached", bridgeCountByType.get("snowflake") >= 1);
                response.put("bridgeCount", bridgeCountByType.get("snowflake"));
                break;
            default:
                response.put("bridgeLimitReached", false);
                response.put("bridgeCount", 0);
        }

        return ResponseEntity.ok(response);
    }


    /**
     * This method is responsible for setting up the bridge configuration.
     * It checks if the bridge limit has been reached and adds this information to the model.
     * The method then returns the name of the setup view.
     *
     * @param model The model for the view.
     * @return The name of the setup view.
     */
    @GetMapping("/setup")
    public String setup(Model model) {
        model.addAttribute("bridgeLimitReached", relayService.getBridgeCount() >= 2);
        return "setup";
    }

    /**
     * This method is responsible for getting the running bridge type.
     * It calls the getRunningBridgeType method on the relay service and returns a response entity with the result.
     *
     * @return ResponseEntity<Map<String, String>> - The response entity containing the map with the running bridge type.
     */
    @GetMapping("/running-type")
    public ResponseEntity<Map<String, String>> getRunningBridgeType() {
        Map<String, String> response = relayService.getRunningBridgeType();
        return ResponseEntity.ok(response);
    }


    /**
     * This method is responsible for reverting the Nginx configuration to its default state.
     * It calls the revertNginxDefaultConfig method on the nginxService.
     * If the operation is successful, it returns a response entity with a success message and HTTP status OK.
     * If an exception occurs during the process, it returns a response entity with an error message and HTTP status INTERNAL_SERVER_ERROR.
     *
     * @return ResponseEntity<String> - The response entity containing the result of the operation and the corresponding HTTP status.
     */
    @PostMapping("/revert-nginx-config")
    public ResponseEntity<String> revertNginxConfig() {
        try {
            nginxService.revertNginxDefaultConfig();
            return new ResponseEntity<>("Nginx configuration reverted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error reverting Nginx configuration: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * This method is responsible for toggling the limit on the number of bridges that can be configured.
     * It calls the static method toggleLimit() on the RelayService class.
     * If the operation is successful, it returns a response entity with HTTP status OK.
     *
     * @return ResponseEntity<Void> - The response entity indicating the result of the operation.
     */
    @PostMapping("/toggle-limit")
    public ResponseEntity<Void> toggleLimit() {
        RelayService.toggleLimit();
        return ResponseEntity.ok().build();
    }

    /**
     * This method is responsible for getting the state of the bridge limit.
     * It calls the static method isLimitOn() on the RelayService class and returns a response entity with the result.
     *
     * @return ResponseEntity<Boolean> - The response entity containing the state of the bridge limit.
     */
    @GetMapping("/limit-state")
    public ResponseEntity<Boolean> getLimitState() {
        return ResponseEntity.ok(RelayService.isLimitOn());
    }
}