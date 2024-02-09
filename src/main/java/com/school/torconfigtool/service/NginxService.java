package com.school.torconfigtool.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class NginxService {

    private static final Logger logger = LoggerFactory.getLogger(NginxService.class);

    public void startNginx() {
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

    public void reloadNginx() {
        System.out.println("Reloading Nginx");
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

    public void generateNginxConfig() {
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
        processBuilder.command("bash", "-c", "sudo systemctl reload nginx");
    }

    public void revertNginxDefaultConfig() {
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
    }

    public boolean isNginxRunning() {
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
}
