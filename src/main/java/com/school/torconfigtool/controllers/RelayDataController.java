package com.school.torconfigtool.controllers;

import com.school.torconfigtool.models.RelayData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class RelayDataController {

    // Sample relay data list (replace this with your actual data source)
    private List<RelayData> relayDataList = new ArrayList<>(); // Make sure this list is populated with data

    @GetMapping("/relay-data")
    public String getRelayData(Model model) {
        // Get the last relay data item (latest data)
        RelayData latestData = relayDataList.isEmpty() ? new RelayData() : relayDataList.get(relayDataList.size() - 1);

        // Add the latest data to the model
        model.addAttribute("latestRelayData", latestData);

        return "relay-data"; // Use the appropriate Thymeleaf template name
    }
}
