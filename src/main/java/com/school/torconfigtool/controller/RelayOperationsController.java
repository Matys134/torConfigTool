package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.RelayOperationsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;

import static com.school.torconfigtool.util.Constants.TORRC_DIRECTORY_PATH;

/**
 * Controller for handling relay operations.
 */
@Controller
@RequestMapping("/relay-operations")
public class RelayOperationsController {

    private final RelayOperationsService relayOperationsService;

    /**
     * Constructor for RelayOperationsController.
     * @param relayOperationsService The service to handle relay operations.
     */
    public RelayOperationsController(RelayOperationsService relayOperationsService) {
        this.relayOperationsService = relayOperationsService;
    }

    /**
     * Handles GET requests to "/relay-operations".
     * @param model The model to be used.
     * @return The name of the view to be rendered.
     */
    @GetMapping
    public String relayOperations(Model model) {
        relayOperationsService.prepareModelForRelayOperationsView(model);
        boolean isSnowflakeConfigured = new File(TORRC_DIRECTORY_PATH + "snowflake_proxy_configured").exists();
        model.addAttribute("isSnowflakeConfigured", isSnowflakeConfigured);
        return "relay-operations";
    }
}