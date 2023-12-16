package com.school.torconfigtool.controllers;

import com.school.torconfigtool.RelayUtils;
import com.school.torconfigtool.config.TorrcConfigurator;
import com.school.torconfigtool.models.BridgeRelayConfig;
import com.school.torconfigtool.service.RelayService;
import com.school.torconfigtool.service.TorrcFileCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/bridge")
public class BridgeController {

    private static final Logger logger = LoggerFactory.getLogger(BridgeController.class);
    private static final String TORRC_DIRECTORY_PATH = "torrc/";
    private static final String TORRC_FILE_PREFIX = "torrc-";
    private static final String NGINX_VHOST_PATH = "/etc/nginx/sites-available/default";
    private final RelayOperationsController relayOperationController;
    private final RelayService relayService;

    public BridgeController(RelayService relayService, RelayOperationsController relayOperationController) {
        this.relayService = relayService;
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
                                  Model model) throws IOException {
        try {
            /*//if bridgeport is null, check only if controlport is available and vice versa
            if (bridgePort == null && !relayService.isPortAvailable(bridgeNickname, bridgeControlPort)) {
                model.addAttribute("errorMessage", "One or more ports are already in use.");
                return "relay-config";
            } else if (bridgeControlPort == 0 && !relayService.isPortAvailable(bridgeNickname, bridgePort)) {
                model.addAttribute("errorMessage", "One or more ports are already in use.");
                return "relay-config";
            } else if (!relayService.arePortsAvailable(bridgeNickname, bridgePort, bridgeControlPort)) {
                model.addAttribute("errorMessage", "One or more ports are already in use.");
                return "relay-config";
            }*/

            String torrcFileName = TORRC_FILE_PREFIX + bridgeNickname + "_bridge";
            Path torrcFilePath = Paths.get(TORRC_DIRECTORY_PATH, torrcFileName).toAbsolutePath().normalize();

            if (RelayUtils.relayExists(bridgeNickname)) {
                model.addAttribute("errorMessage", "A relay with the same nickname already exists.");
                return "relay-config";
            }

            /*if (!RelayUtils.portsAreAvailable(bridgeNickname, bridgePort, bridgeControlPort)) {
                model.addAttribute("errorMessage", "One or more ports are already in use.");
                return "relay-config";
            }*/

            BridgeRelayConfig config = createBridgeConfig(bridgeNickname, bridgePort, bridgeContact, bridgeControlPort, bridgeBandwidth);
            if (!torrcFilePath.toFile().exists()) {
                TorrcFileCreator.createTorrcFile(torrcFilePath.toString(), config);
            }

            model.addAttribute("successMessage", "Tor Relay configured successfully!");
        } catch (Exception e) {
            logger.error("Error during Tor Relay configuration", e);
            model.addAttribute("errorMessage", "Failed to configure Tor Relay.");
        }

        if (webtunnelUrl != null && !webtunnelUrl.isEmpty()) {
            generateNginxConfig(80);
            setupWebtunnel(webtunnelUrl);
            installCert(webtunnelUrl);
            modifyNginxConfig();
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

    private BridgeRelayConfig createBridgeConfig(String bridgeNickname, Integer bridgePort, String bridgeContact, int bridgeControlPort, Integer bridgeBandwidth) {
        BridgeRelayConfig config = new BridgeRelayConfig();
        config.setNickname(bridgeNickname);
        if (bridgePort != null)
            config.setOrPort(String.valueOf(bridgePort));
        config.setContact(bridgeContact);
        config.setControlPort(String.valueOf(bridgeControlPort));
        if (bridgeBandwidth != null)
            config.setBandwidthRate(String.valueOf(bridgeBandwidth));


        return config;
    }

    private void setupWebtunnel(String webTunnelUrl) {
        String programLocation = System.getProperty("user.dir");
        String command = "/home/matys/.acme.sh/acme.sh --issue -d " + webTunnelUrl + " -w " + programLocation + "/torConfigTool/onion/www/service-80/";

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("Error during webtunnel setup. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error during webtunnel setup", e);
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

    private void modifyNginxConfig() {
        String programLocation = System.getProperty("user.dir");
        String nginxConfigPath = "/etc/nginx/sites-available/webtunnel";

        // Generate random string
        String command = "echo $(cat /dev/urandom | tr -cd \"qwertyuiopasdfghjklzxcvbnmMNBVCXZLKJHGFDSAQWERTUIOP0987654321\"|head -c 24)";
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);
        String randomString = "";
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            randomString = reader.readLine();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            logger.error("Error during random string generation", e);
        }

        // Modify nginx configuration
        String configContent = "server {\n" +
                "    listen 443 ssl;\n" +
                "    listen [::]:443 ssl;\n\n" +
                "    server_name webtunnel;\n\n" +
                "    index index.html" +
                "    root " + programLocation + "/torConfigTool/onion/www/service-80;\n\n" +
                "    ssl_certificate " + programLocation + "/torConfigTool/onion/certs/service-80/fullchain.pem;\n" +
                "    ssl_certificate_key " + programLocation + "/torConfigTool/onion/certs/service-80/key.pem;\n\n" +
                "    location /" + randomString + " {\n" +
                "        proxy_pass http://127.0.0.1:15000;\n" +
                "        proxy_http_version 1.1;\n" +
                "        proxy_set_header Upgrade $http_upgrade;\n" +
                "        proxy_set_header Connection \"upgrade\";\n" +
                "        proxy_set_header Accept-Encoding \"\";\n" +
                "        proxy_set_header Host $host;\n" +
                "        proxy_set_header X-Real-IP $remote_addr;\n" +
                "        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n" +
                "        proxy_set_header X-Forwarded-Proto $scheme;\n" +
                "        add_header Front-End-Https on;\n" +
                "        proxy_redirect off;\n" +
                "        access_log off;\n" +
                "        error_log off;\n" +
                "    }\n" +
                "}";

        try (FileWriter fileWriter = new FileWriter(nginxConfigPath, false);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(configContent);
        } catch (IOException e) {
            logger.error("Error during nginx configuration modification", e);
        }
    }

    private void generateNginxConfig(int onionServicePort) throws IOException {
        try {

            String currentDirectory = System.getProperty("user.dir");

            File wwwDir = new File(currentDirectory + "/onion/www");
            if (!wwwDir.exists() && !wwwDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + wwwDir.getAbsolutePath());
            }

            File serviceDir = new File(wwwDir, "service-" + onionServicePort);
            if (!serviceDir.exists() && !serviceDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + serviceDir.getAbsolutePath());
            }


            File indexHtml = new File(serviceDir, "index.html");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(indexHtml))) {
                writer.write("<html><body><h1>Test Onion Service</h1></body></html>");
            }
        }
        catch (IOException e) {
            logger.error("Error generating Nginx configuration", e);
        }
    }
}