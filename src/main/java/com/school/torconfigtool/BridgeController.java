package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/bridge")
public class BridgeController {

    private static final Logger logger = LoggerFactory.getLogger(BridgeController.class);
    private final RelayService relayService;
    private final NginxService nginxService;
    private final FileService fileService;
    private final BridgeService bridgeService;

    @Autowired
    public BridgeController(RelayService relayService, NginxService nginxService, FileService fileService, BridgeService bridgeService) {
        this.relayService = relayService;
        this.nginxService = nginxService;
        this.fileService = fileService;
        this.bridgeService = bridgeService;
    }

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
        if (relayService.getBridgeCount() >= 2) {
            model.addAttribute("errorMessage", "You can only configure up to 2 bridges.");
            return "setup";
        }
        try {
            bridgeService.configureBridgeInternal(bridgeType, bridgePort, bridgeTransportListenAddr, bridgeContact, bridgeNickname, webtunnelDomain, bridgeControlPort, webtunnelUrl, webtunnelPort, startBridgeAfterConfig, bridgeBandwidth, model);
        } catch (Exception e) {
            logger.error("Error during Tor Relay configuration", e);
            model.addAttribute("errorMessage", "Failed to configure Tor Relay.");
        }
        return "setup";
    }

    @PostMapping("/run-snowflake-proxy")
    public ResponseEntity<String> runSnowflakeProxy() {
        try {
            BridgeRelayConfig bridgeRelayConfig = new BridgeRelayConfig();
            bridgeRelayConfig.runSnowflakeProxy();
            return new ResponseEntity<>("Snowflake proxy started successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error starting snowflake proxy: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

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

    @GetMapping("/setup")
    public String setup(Model model) {
        model.addAttribute("bridgeLimitReached", relayService.getBridgeCount() >= 2);
        return "setup";
    }

    @GetMapping("/running-type")
    public ResponseEntity<Map<String, String>> getRunningBridgeType() {
        Map<String, String> response = relayService.getRunningBridgeType();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/revert-nginx-config")
    public ResponseEntity<String> revertNginxConfig() {
        try {
            nginxService.revertNginxDefaultConfig();
            return new ResponseEntity<>("Nginx configuration reverted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error reverting Nginx configuration: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/toggle-limit")
    public ResponseEntity<Void> toggleLimit() {
        RelayService.toggleLimit();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/limit-state")
    public ResponseEntity<Boolean> getLimitState() {
        return ResponseEntity.ok(RelayService.isLimitOn());
    }

    @PostMapping("/upload/{port}")
    public String uploadFiles(@RequestParam("files") MultipartFile[] files, @PathVariable("port") int port, Model model) {
        try {
            String fileDir = "onion/www/service-" + port + "/";
            fileService.uploadFiles(files, fileDir);
            List<String> fileNames = fileService.getUploadedFiles(fileDir);
            model.addAttribute("uploadedFiles", fileNames);
            model.addAttribute("message", "Files uploaded successfully!");
            return "file_upload_form";
        } catch (Exception e) {
            model.addAttribute("message", "Fail! -> uploaded filename: " + Arrays.toString(files));
            return "file_upload_form";
        }
    }

    @PostMapping("/remove-files/{port}")
    public String removeFiles(@RequestParam("selectedFiles") String[] fileNames, @PathVariable("port") int port, Model model) {
        try {
            String fileDir = "onion/www/service-" + port + "/";
            for (String fileName : fileNames) {
                fileService.deleteFile(fileName, fileDir);
            }
            List<String> remainingFileNames = fileService.getUploadedFiles(fileDir);
            model.addAttribute("uploadedFiles", remainingFileNames);
            model.addAttribute("message", "Files deleted successfully.");
            return "file_upload_form";
        } catch (Exception e) {
            model.addAttribute("message", "Error: " + e.getMessage());
            return "file_upload_form";
        }
    }

    @GetMapping("/upload/{port}")
    public String showUploadForm(@PathVariable("port") int port, Model model) {
        List<String> fileNames = bridgeService.getUploadedFiles(port);
        model.addAttribute("uploadedFiles", fileNames);
        return "file_upload_form";
    }
}