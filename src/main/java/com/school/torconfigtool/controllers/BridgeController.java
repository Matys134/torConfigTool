package com.school.torconfigtool.controllers;

import com.school.torconfigtool.RelayUtils;
import com.school.torconfigtool.models.BridgeRelayConfig;
import com.school.torconfigtool.service.TorrcFileCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/bridge")
public class BridgeController {

    private static final Logger logger = LoggerFactory.getLogger(BridgeController.class);
    private static final String TORRC_DIRECTORY_PATH = "torrc/";
    private static final String TORRC_FILE_PREFIX = "torrc-";
    private final RelayOperationsController relayOperationController;

    public BridgeController(RelayOperationsController relayOperationController) {
        this.relayOperationController = relayOperationController;
    }

    @GetMapping
    public String bridgeConfigurationForm() {
        return "relay-config";
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
        try {

            String torrcFileName = TORRC_FILE_PREFIX + bridgeNickname + "_bridge";
            Path torrcFilePath = Paths.get(TORRC_DIRECTORY_PATH, torrcFileName).toAbsolutePath().normalize();

            if (RelayUtils.relayExists(bridgeNickname)) {
                model.addAttribute("errorMessage", "A relay with the same nickname already exists.");
                return "relay-config";
            }

            BridgeRelayConfig config = createBridgeConfig(bridgeTransportListenAddr, bridgeType, bridgeNickname, bridgePort, bridgeContact, bridgeControlPort, bridgeBandwidth, webtunnelDomain, webtunnelUrl, webtunnelPort);
            if (!torrcFilePath.toFile().exists()) {
                TorrcFileCreator.createTorrcFile(torrcFilePath.toString(), config);
            }

            model.addAttribute("successMessage", "Tor Relay configured successfully!");
        } catch (Exception e) {
            logger.error("Error during Tor Relay configuration", e);
            model.addAttribute("errorMessage", "Failed to configure Tor Relay.");
        }

        if (webtunnelUrl != null && !webtunnelUrl.isEmpty()) {
            generateNginxConfig();
            setupWebtunnel(webtunnelUrl);
            installCert(webtunnelUrl);
            String randomString = UUID.randomUUID().toString().replace("-", "").substring(0, 24);
            modifyNginxDefaultConfig(System.getProperty("user.dir"), randomString);
        }

        if (startBridgeAfterConfig) {
            try {
                relayOperationController.startRelay(bridgeNickname, "bridge", model);
                model.addAttribute("successMessage", "Tor Relay configured and started successfully!");
            } catch (Exception e) {
                logger.error("Error starting Tor Relay", e);
                model.addAttribute("errorMessage", "Failed to start Tor Relay.");
            }
        }

        return "relay-config";
    }

    private BridgeRelayConfig createBridgeConfig(Integer bridgeTransportListenAddr, String bridgeType, String bridgeNickname, Integer bridgePort, String bridgeContact, int bridgeControlPort, Integer bridgeBandwidth, String webtunnelDomain, String webtunnelUrl, Integer webtunnelPort) {
        BridgeRelayConfig config = new BridgeRelayConfig();
        config.setBridgeType(bridgeType);
        config.setNickname(bridgeNickname);
        if (bridgePort != null)
            config.setOrPort(String.valueOf(bridgePort));
        config.setContact(bridgeContact);
        config.setControlPort(String.valueOf(bridgeControlPort));
        if (bridgeBandwidth != null)
            config.setBandwidthRate(String.valueOf(bridgeBandwidth));
        if (webtunnelDomain != null)
            config.setWebtunnelDomain(webtunnelDomain);
        if (webtunnelUrl != null)
            config.setWebtunnelUrl(webtunnelUrl);
        if (webtunnelPort != null)
            config.setWebtunnelPort(webtunnelPort);
        if (bridgeTransportListenAddr != null)
            config.setBridgeTransportListenAddr(String.valueOf(bridgeTransportListenAddr));


        return config;
    }

    private void setupWebtunnel(String webTunnelUrl) {
        String programLocation = System.getProperty("user.dir");

        // Change the ownership of the directory
        String chownCommand = "sudo chown -R matys:matys " + programLocation + "/onion/www/service-80";
        executeCommand(chownCommand);

        String installAcme = " curl https://get.acme.sh | sh -s email=koubamates4@gmail.com";
        System.out.println("Installing acme.sh" + installAcme);
        executeCommand(installAcme);

        // Create the directory for the certificate files
        String certDirectory = programLocation + "/onion/certs/service-80/";
        new File(certDirectory).mkdirs();

        String command = "/home/matys/.acme.sh/acme.sh --issue -d www." + webTunnelUrl + " -w " + programLocation + "/onion/www/service-80/ --nginx";
        System.out.println("Generating certificate: " + command);

        executeCommand(command);
    }

    private Process executeCommand(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("Error during command execution. Exit code: " + exitCode);
            }
            return process;
        } catch (IOException | InterruptedException e) {
            logger.error("Error during command execution", e);
            return null;
        }
    }

    private void installCert(String webTunnelUrl) {
        String programLocation = System.getProperty("user.dir");
        String command = "/home/matys/.acme.sh/acme.sh --install-cert -d " + webTunnelUrl +
                " --key-file " + programLocation + "/onion/certs/service-80/key.pem" +
                " --fullchain-file " + programLocation + "/onion/certs/service-80/fullchain.pem" +
                " --reloadcmd \"sudo systemctl restart nginx.service\"";

        System.out.println(command);

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("Error during certificate installation. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error during certificate installation", e);
        }
    }

    public void modifyNginxDefaultConfig(String programLocation, String randomString) {
        Path defaultConfigPath = Paths.get("/etc/nginx/sites-available/default");

        try {
            // Read the file into a list of strings
            List<String> lines = Files.readAllLines(defaultConfigPath);

            // Uncomment the lines
            lines = lines.stream()
                    .map(line -> line.replace("# listen 443 ssl default_server;", "listen 443 ssl default_server;"))
                    .map(line -> line.replace("# listen [::]:443 ssl default_server;", "listen [::]:443 ssl default_server;"))
                    .collect(Collectors.toList());

            // Find the line with the root path and change it
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).trim().startsWith("root")) {
                    lines.set(i, "root " + programLocation + "/torConfigTool/onion/www/service-80;");
                    break;
                }
            }

            // Find the line with the listen [::]:443 ssl default_server; and add the new lines
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).trim().equals("listen [::]:443 ssl default_server;")) {
                    lines.add(i + 1, "ssl_certificate " + programLocation + "/torConfigTool/onion/certs/service-80/fullchain.pem;");
                    lines.add(i + 2, "ssl_certificate_key " + programLocation + "/torConfigTool/onion/certs/service-80/key.pem;");
                    break;
                }
            }

            // Find the location block and replace its content
            int locationStartIndex = -1;
            int locationEndIndex = -1;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).trim().startsWith("location /")) {
                    locationStartIndex = i;
                }
                if (lines.get(i).trim().equals("}") && locationStartIndex != -1) {
                    locationEndIndex = i;
                    break;
                }
            }

            if (locationStartIndex != -1 && locationEndIndex != -1) {
                lines.subList(locationStartIndex + 1, locationEndIndex).clear();
                lines.add(locationStartIndex + 1, "proxy_pass http://127.0.0.1:15000;");
                lines.add(locationStartIndex + 2, "proxy_http_version 1.1;");
                lines.add(locationStartIndex + 3, "proxy_set_header Upgrade $http_upgrade;");
                lines.add(locationStartIndex + 4, "proxy_set_header Connection \"upgrade\";");
                lines.add(locationStartIndex + 5, "proxy_set_header Accept-Encoding \"\";");
                lines.add(locationStartIndex + 6, "proxy_set_header Host $host;");
                lines.add(locationStartIndex + 7, "proxy_set_header X-Real-IP $remote_addr;");
                lines.add(locationStartIndex + 8, "proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;");
                lines.add(locationStartIndex + 9, "proxy_set_header X-Forwarded-Proto $scheme;");
                lines.add(locationStartIndex + 10, "add_header Front-End-Https on;");
                lines.add(locationStartIndex + 11, "proxy_redirect off;");
                lines.add(locationStartIndex + 12, "access_log off;");
                lines.add(locationStartIndex + 13, "error_log off;");
            }

            // Write the list back to the file
            Files.write(defaultConfigPath, lines);
        } catch (IOException e) {
            logger.error("Error modifying Nginx default configuration", e);
        }
    }

    private void generateNginxConfig() {
        try {

            String currentDirectory = System.getProperty("user.dir");

            File indexHtml = getFile(currentDirectory);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(indexHtml))) {
                writer.write("<html><body><h1>Test Onion Service</h1></body></html>");
            }
        } catch (IOException e) {
            logger.error("Error generating Nginx configuration", e);
        }
    }

    private File getFile(String currentDirectory) throws IOException {
        File wwwDir = new File(currentDirectory + "/onion/www");
        if (!wwwDir.exists() && !wwwDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + wwwDir.getAbsolutePath());
        }

        File serviceDir = new File(wwwDir, "service-" + 80);
        if (!serviceDir.exists() && !serviceDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + serviceDir.getAbsolutePath());
        }


        return new File(serviceDir, "index.html");
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


}