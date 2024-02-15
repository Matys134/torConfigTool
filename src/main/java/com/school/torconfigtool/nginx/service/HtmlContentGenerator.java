package com.school.torconfigtool.nginx.service;

import org.springframework.stereotype.Service;

/**
 * HtmlContentGenerator is a service class responsible for generating HTML content.
 * Currently, it only generates a simple HTML structure with a single header.
 *
 * This class is annotated with @Service to indicate that it's a service component in Spring framework.
 * This means that Spring will automatically handle the lifecycle of instances of this class.
 */
@Service
public class HtmlContentGenerator {

    /**
     * Generates a simple HTML content.
     *
     * The generated HTML content is a full HTML document with a single header.
     * The header text is "Test Onion Service".
     *
     * @return A string representing the HTML content.
     */
    public String generateHtmlContent() {
        return "<html><body><h1>Test Onion Service</h1></body></html>";
    }
}