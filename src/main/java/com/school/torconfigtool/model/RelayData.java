package com.school.torconfigtool.model;

import lombok.Data;

import java.util.List;

/**
 * This class represents the data related to a relay in the Tor network.
 * It uses Lombok's @Data annotation to automatically generate getters, setters, equals, hashCode, and toString methods.
 */
@Data
public class RelayData {
    // The download speed of the relay
    private double download;
    // The upload speed of the relay
    private double upload;
    // The bandwidth of the relay
    private double bandwidth;
    // The uptime of the relay
    private double uptime;
    // The flags associated with the relay
    private List<String> flags;
    // The event associated with the relay
    private String event;
    // The version of the relay
    private String version;
}