package com.school.torconfigtool.nginx.service;

import com.school.torconfigtool.NginxFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This service class is responsible for generating the Nginx configuration.
 */
@Service
public class NginxConfigGenerator {

    private static final Logger logger = LoggerFactory.getLogger(NginxConfigGenerator.class);

    private final NginxFileService nginxFileService;
    private final HtmlContentGenerator htmlContentGenerator;

    /**
     * Constructor for the NginxConfigGenerator.
     * @param nginxFileService Service for handling Nginx file operations.
     * @param htmlContentGenerator Service for generating HTML content.
     */
    public NginxConfigGenerator(NginxFileService nginxFileService, HtmlContentGenerator htmlContentGenerator) {
        this.nginxFileService = nginxFileService;
        this.htmlContentGenerator = htmlContentGenerator;
    }

    /**
     * Generates the Nginx configuration.
     * Retrieves the file from the current directory and writes the generated HTML content to it.
     * If an IOException occurs, it logs the error and throws a RuntimeException.
     */
    public void generateNginxConfig() {
        try {
            File indexHtml = retrieveFile();
            writeToFile(indexHtml);
        } catch (IOException e) {
            logger.error("Error generating Nginx configuration", e);
            throw new RuntimeException("Error generating Nginx configuration", e);
        }
    }

    /**
     * Retrieves the file from the current directory.
     * @return The retrieved file.
     * @throws IOException If an error occurs during file retrieval.
     */
    private File retrieveFile() throws IOException {
        String currentDirectory = System.getProperty("user.dir");
        return nginxFileService.getFile(currentDirectory);
    }

    /**
     * Writes the generated HTML content to the given file.
     * @param file The file to write to.
     * @throws IOException If an error occurs during writing to the file.
     */
    private void writeToFile(File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(htmlContentGenerator.generateHtmlContent());
        }
    }

    /**
     * Generates common configuration lines for Nginx.
     * @param rootDirectory The root directory for the server.
     * @param listenPort The port the server should listen on.
     * @param sslCertificate The SSL certificate file path.
     * @param sslCertificateKey The SSL certificate key file path.
     * @return A list of configuration lines.
     */
    public List<String> generateCommonConfig(String rootDirectory, String listenPort, String sslCertificate, String sslCertificateKey) {
        List<String> lines = new ArrayList<>();
        lines.add("server {");
        lines.add("    listen " + listenPort + " default_server;");
        lines.add("    listen [::]:" + listenPort + " default_server;");
        lines.add("    root " + rootDirectory + ";");
        lines.add("    index index.html index.htm index.nginx-debian.html;");
        lines.add("    server_name _;");
        if (sslCertificate != null && sslCertificateKey != null) {
            lines.add("    ssl_certificate " + sslCertificate + ";");
            lines.add("    ssl_certificate_key " + sslCertificateKey + ";");
        }
        lines.add("    location / {");
        lines.add("        try_files $uri $uri/ =404;");
        lines.add("    }");
        lines.add("}");
        return lines;
    }

    /**
     * Generates final configuration lines for Nginx.
     * @param programLocation The location of the program.
     * @param randomString A random string used in the location directive.
     * @return A list of configuration lines.
     */
    public List<String> generateFinalConfigLines(String programLocation, String randomString) {
        List<String> lines = generateCommonConfig(programLocation + "/onion/www/service-80", "443", null, null);
        lines.addAll(generateSSLConfigLines(programLocation));
        lines.addAll(generateLocationLines(randomString));
        lines.add("}");
        return lines;
    }

    /**
     * Generates SSL configuration lines for Nginx.
     * @param programLocation The location of the program.
     * @return A list of configuration lines.
     */
    public List<String> generateSSLConfigLines(String programLocation) {
        List<String> lines = new ArrayList<>();
        lines.add("    ssl_certificate " + programLocation + "/onion/certs/service-80/fullchain.pem;");
        lines.add("    ssl_certificate_key " + programLocation + "/onion/certs/service-80/key.pem;");
        return lines;
    }

    /**
     * Generates location lines for Nginx.
     * @param randomString A random string used in the location directive.
     * @return A list of configuration lines.
     */
    public List<String> generateLocationLines(String randomString) {
        List<String> lines = new ArrayList<>();
        lines.add("    location = /" + randomString + " {");
        lines.add("        proxy_pass http://127.0.0.1:15000;");
        lines.add("        proxy_http_version 1.1;");
        lines.add("        proxy_set_header Upgrade $http_upgrade;");
        lines.add("        proxy_set_header Connection \"upgrade\";");
        lines.addAll(generateProxyHeaders());
        lines.add("        add_header Front-End-Https on;");
        lines.add("        proxy_redirect off;");
        lines.add("        access_log off;");
        lines.add("        error_log off;");
        lines.add("    }");
        return lines;
    }

    /**
     * Generates proxy headers for Nginx.
     * @return A list of configuration lines.
     */
    public List<String> generateProxyHeaders() {
        List<String> lines = new ArrayList<>();
        lines.add("        proxy_set_header Accept-Encoding \"\";");
        lines.add("        proxy_set_header Host $host;");
        lines.add("        proxy_set_header X-Real-IP $remote_addr;");
        lines.add("        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;");
        lines.add("        proxy_set_header X-Forwarded-Proto $scheme;");
        return lines;
    }
}