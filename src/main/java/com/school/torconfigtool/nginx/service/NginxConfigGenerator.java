package com.school.torconfigtool.nginx.service;

import com.school.torconfigtool.NginxFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
}