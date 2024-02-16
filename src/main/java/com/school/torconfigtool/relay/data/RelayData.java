package com.school.torconfigtool.relay.data;

import lombok.Data;
import java.util.List;

/**
 * The RelayData class represents the data related to a relay in the Tor network.
 * It uses the @Data annotation from Lombok to automatically provide getter, setter, equals, hashCode, and toString methods.
 *
 * @author Matys134
 */
@Data
public class RelayData {
    /**
     * The download speed of the relay in Mbps.
     */
    private double download;

    /**
     * The upload speed of the relay in Mbps.
     */
    private double upload;

    /**
     * The total bandwidth of the relay in Mbps.
     */
    private double bandwidth;

    /**
     * The uptime of the relay in hours.
     */
    private double uptime;

    /**
     * The flags associated with the relay. These can include flags such as "Fast", "Guard", "Exit", etc.
     */
    private List<String> flags;

    /**
     * The event associated with the relay. This could be an event such as "Connected", "Disconnected", etc.
     */
    private String event;

    /**
     * The version of the Tor software that the relay is running.
     */
    private String version;
}