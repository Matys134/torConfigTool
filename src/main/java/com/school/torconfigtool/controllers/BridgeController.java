package com.school.torconfigtool.controllers;

import com.school.torconfigtool.RelayUtils;
import com.school.torconfigtool.models.BridgeRelayConfig;
import com.school.torconfigtool.service.FileService;
import com.school.torconfigtool.service.RelayService;
import com.school.torconfigtool.service.TorrcFileCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
@RequestMapping("/bridge")
public class BridgeController {

    @Autowired
    private FileService fileService;

    private static final Logger logger = LoggerFactory.getLogger(BridgeController.class);
    private static final String TORRC_DIRECTORY_PATH = "torrc/";
    private static final String TORRC_FILE_PREFIX = "torrc-";
    private final RelayService relayService;
    private final RelayOperationsController relayOperationController;

    public BridgeController(RelayService relayService, RelayOperationsController relayOperationController) {
        this.relayService = relayService;
        this.relayOperationController = relayOperationController;
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
            configureBridgeInternal(bridgeType, bridgePort, bridgeTransportListenAddr, bridgeContact, bridgeNickname, webtunnelDomain, bridgeControlPort, webtunnelUrl, webtunnelPort, startBridgeAfterConfig, bridgeBandwidth, model);
        } catch (Exception e) {
            logger.error("Error during Tor Relay configuration", e);
            model.addAttribute("errorMessage", "Failed to configure Tor Relay.");
        }
        return "setup";
    }

    private void configureBridgeInternal(String bridgeType, Integer bridgePort, Integer bridgeTransportListenAddr, String bridgeContact, String bridgeNickname, String webtunnelDomain, int bridgeControlPort, String webtunnelUrl, Integer webtunnelPort, boolean startBridgeAfterConfig, Integer bridgeBandwidth, Model model) throws Exception {
        String torrcFileName = TORRC_FILE_PREFIX + bridgeNickname + "_bridge";
        Path torrcFilePath = Paths.get(TORRC_DIRECTORY_PATH, torrcFileName).toAbsolutePath().normalize();

        if (RelayUtils.relayExists(bridgeNickname)) {
            model.addAttribute("errorMessage", "A relay with the same nickname already exists.");
            return;
        }

        // Log the bridgeType before creating the BridgeRelayConfig object
        logger.info("Bridge type before creating BridgeRelayConfig: " + bridgeType);

        BridgeRelayConfig config = createBridgeConfig(bridgeTransportListenAddr, bridgeType, bridgeNickname, bridgePort, bridgeContact, bridgeControlPort, bridgeBandwidth, webtunnelDomain, webtunnelUrl, webtunnelPort);

        // Log the bridgeType after creating the BridgeRelayConfig object
        logger.info("Bridge type after creating BridgeRelayConfig: " + config.getBridgeType());

        if (!torrcFilePath.toFile().exists()) {
            TorrcFileCreator.createTorrcFile(torrcFilePath.toString(), config);
        }

        model.addAttribute("successMessage", "Tor Relay configured successfully!");

        if (webtunnelUrl != null && !webtunnelUrl.isEmpty()) {
            generateNginxConfig();
            changeRootDirectory(System.getProperty("user.dir") + "/onion/www/service-80");
            setupWebtunnel(webtunnelUrl);
            String randomString = UUID.randomUUID().toString().replace("-", "").substring(0, 24);
            modifyNginxDefaultConfig(System.getProperty("user.dir"), randomString, webtunnelUrl);
            config.setPath(randomString); // Set the path
            updateTorrcFile(config); // Update the torrc file

            // Restart nginx
            relayOperationController.reloadNginx();
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
    }

    private BridgeRelayConfig createBridgeConfig(Integer bridgeTransportListenAddr, String bridgeType, String bridgeNickname, Integer bridgePort, String bridgeContact, int bridgeControlPort, Integer bridgeBandwidth, String webtunnelDomain, String webtunnelUrl, Integer webtunnelPort) {
        BridgeRelayConfig config = new BridgeRelayConfig();
        config.setBridgeType(bridgeType);

        // Log the bridgeType after setting it in the BridgeRelayConfig object
        logger.info("Bridge type after setting in BridgeRelayConfig: " + config.getBridgeType());

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
            config.setServerTransport(String.valueOf(bridgeTransportListenAddr));

        return config;
    }

    private void setupWebtunnel(String webTunnelUrl) throws Exception {

        if (!isNginxRunning()) {
            startNginx();
        }

        String programLocation = System.getProperty("user.dir");

        // Change the ownership of the directory
        String chownCommand = "sudo chown -R matys:matys " + programLocation + "/onion/www/service-80";
        Process chownProcess = executeCommand(chownCommand);
        if (chownProcess == null || chownProcess.exitValue() != 0) {
            throw new Exception("Failed to change ownership of the directory");
        }

        // Create the directory for the certificate files
        String certDirectory = programLocation + "/onion/certs/service-80/";
        new File(certDirectory).mkdirs();

        String command = "/home/matys/.acme.sh/acme.sh --issue -d " + webTunnelUrl + " -w " + programLocation + "/onion/www/service-80/ --nginx --server letsencrypt_test --force";
        System.out.println("Generating certificate: " + command);

        Process certProcess = executeCommand(command);
        if (certProcess == null || certProcess.exitValue() != 0) {
            throw new Exception("Failed to generate certificate");
        }
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
        String command = "/home/matys/.acme.sh/acme.sh --install-cert -d " + webTunnelUrl + " -d " + webTunnelUrl +
                " --key-file " + programLocation + "/onion/certs/service-80/key.pem" +
                " --fullchain-file " + programLocation + "/onion/certs/service-80/fullchain.pem" +
                " --reloadcmd";

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

    public void modifyNginxDefaultConfig(String programLocation, String randomString, String webTunnelUrl) {
        Path defaultConfigPath = Paths.get("/etc/nginx/sites-available/default");

        try {
            // Clear the file and write the initial configuration
            List<String> lines = new ArrayList<>();
            lines.add("server {");
            lines.add("    listen 80 default_server;");
            lines.add("    listen [::]:80 default_server;");
            lines.add("    root " + programLocation + "/onion/www/service-80;");
            lines.add("    index index.html index.htm index.nginx-debian.html;");
            lines.add("    server_name _;");
            lines.add("    location / {");
            lines.add("        try_files $uri $uri/ =404;");
            lines.add("    }");
            lines.add("}");

            // Write the list to the file
            Files.write(defaultConfigPath, lines);

            // Issue and install the certificates
            installCert(webTunnelUrl);

            // Read the file into a list of strings again
            lines = Files.readAllLines(defaultConfigPath);

            // Clear the list and add the new configuration lines
            lines.clear();
            lines.add("server {");
            lines.add("    listen [::]:443 ssl http2;");
            lines.add("    listen 443 ssl http2;");
            lines.add("    root " + programLocation + "/onion/www/service-80;");
            lines.add("    index index.html index.htm index.nginx-debian.html;");
            lines.add("    server_name $SERVER_ADDRESS;");
            lines.add("    ssl_certificate " + programLocation + "/onion/certs/service-80/fullchain.pem;");
            lines.add("    ssl_certificate_key " + programLocation + "/onion/certs/service-80/key.pem;");
            // Add the rest of the configuration lines...
            lines.add("    location = /" + randomString + " {");
            lines.add("        proxy_pass http://127.0.0.1:15000;");
            lines.add("        proxy_http_version 1.1;");
            lines.add("        proxy_set_header Upgrade $http_upgrade;");
            lines.add("        proxy_set_header Connection \"upgrade\";");
            lines.add("        proxy_set_header Accept-Encoding \"\";");
            lines.add("        proxy_set_header Host $host;");
            lines.add("        proxy_set_header X-Real-IP $remote_addr;");
            lines.add("        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;");
            lines.add("        proxy_set_header X-Forwarded-Proto $scheme;");
            lines.add("        add_header Front-End-Https on;");
            lines.add("        proxy_redirect off;");
            lines.add("        access_log off;");
            lines.add("        error_log off;");
            lines.add("    }");
            lines.add("}");

            // Write the list back to the file
            Files.write(defaultConfigPath, lines);
        } catch (IOException e) {
            logger.error("Error modifying Nginx default configuration", e);
        }

        reloadNginx();
    }

    private boolean isNginxRunning() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "systemctl is-active nginx");
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            logger.error("Error checking Nginx status", e);
            return false;
        }
    }

    private void startNginx() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "sudo systemctl start nginx");
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("Error starting Nginx. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error starting Nginx", e);
        }
    }

    private void reloadNginx() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "sudo systemctl reload nginx");
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("Error reloading Nginx. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error reloading Nginx", e);
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

    private void revertNginxDefaultConfig() {
        Path defaultConfigPath = Paths.get("/etc/nginx/sites-available/default");

        try {
            // Clear the file and write the initial configuration
            List<String> lines = new ArrayList<>();
            lines.add("server {");
            lines.add("    listen 80 default_server;");
            lines.add("    listen [::]:80 default_server;");
            lines.add("    root /home/matys/git/torConfigTool/onion/www/service-80;");
            lines.add("    index index.html index.htm index.nginx-debian.html;");
            lines.add("    server_name _;");
            lines.add("    location / {");
            lines.add("        try_files $uri $uri/ =404;");
            lines.add("    }");
            lines.add("}");

            // Write the list to the file
            Files.write(defaultConfigPath, lines);
        } catch (IOException e) {
            logger.error("Error reverting Nginx default configuration", e);
        }
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
        return ResponseEntity.ok(relayService.getRunningBridgeType());
    }

    @PostMapping("/revert-nginx-config")
    public ResponseEntity<String> revertNginxConfig() {
        try {
            revertNginxDefaultConfig();
            return new ResponseEntity<>("Nginx configuration reverted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error reverting Nginx configuration: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void updateTorrcFile(BridgeRelayConfig config) throws IOException {
        String torrcFileName = TORRC_FILE_PREFIX + config.getNickname() + "_bridge";
        Path torrcFilePath = Paths.get(TORRC_DIRECTORY_PATH, torrcFileName).toAbsolutePath().normalize();

        List<String> lines = Files.readAllLines(torrcFilePath);
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("ServerTransportOptions webtunnel url")) {
                lines.set(i, "ServerTransportOptions webtunnel url=https://" + config.getWebtunnelUrl() + "/" + config.getPath());
                break;
            }
        }
        Files.write(torrcFilePath, lines);
    }

    // method to change root directory of nginx in default config file
    public void changeRootDirectory(String rootDirectory) {
        Path defaultConfigPath = Paths.get("/etc/nginx/sites-available/default");

        try {
            // Read the file into a list of strings
            List<String> lines = Files.readAllLines(defaultConfigPath);

            // Clear the list and add the new configuration lines
            lines.clear();
            lines.add("server {");
            lines.add("    listen 80 default_server;");
            lines.add("    listen [::]:80 default_server;");
            lines.add("    root " + rootDirectory + ";");
            lines.add("    index index.html index.htm index.nginx-debian.html;");
            lines.add("    server_name _;");
            lines.add("    location / {");
            lines.add("        try_files $uri $uri/ =404;");
            lines.add("    }");
            lines.add("}");

            // Write the list back to the file
            Files.write(defaultConfigPath, lines);
        } catch (IOException e) {
            logger.error("Error modifying Nginx default configuration", e);
        }
        // Restart the nginx service
        ProcessBuilder processBuilder = new ProcessBuilder();
        // Restart the nginx service using kill command and then start command
        processBuilder.command("bash", "-c", "sudo killall nginx && sudo nginx");
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

    private List<String> getUploadedFiles(int port) {
        String uploadDir = "onion/www/service-" + port + "/";
        return fileService.getUploadedFiles(uploadDir);
    }

    @GetMapping("/upload/{port}")
    public String showUploadForm(@PathVariable("port") int port, Model model) {
        List<String> fileNames = getUploadedFiles(port);
        model.addAttribute("uploadedFiles", fileNames);
        return "file_upload_form";
    }
}