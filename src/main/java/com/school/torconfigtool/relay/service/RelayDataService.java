package com.school.torconfigtool.relay.service;

import com.school.torconfigtool.relay.data.RelayData;
import com.school.torconfigtool.util.QueueManager;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service class for managing relay data and events.
 */
@Service
public class RelayDataService {

    // Map to store relay data queues, keyed by relay ID
    private final Map<Integer, Deque<RelayData>> relayDataMap = new ConcurrentHashMap<>();
    // Map to store relay event queues, keyed by relay ID
    private final Map<Integer, Deque<String>> relayEventMap = new ConcurrentHashMap<>();
    // Queue manager to handle adding data and events to the queues
    private final QueueManager queueManager = new QueueManager();

    /**
     * Adds relay data to the queue for the specified relay ID.
     *
     * @param relayId   the ID of the relay
     * @param relayData the relay data to add
     */
    public void addRelayData(int relayId, RelayData relayData) {
        Deque<RelayData> relayDataQueue = relayDataMap.computeIfAbsent(relayId, k -> new LinkedList<>());
        queueManager.addData(relayDataQueue, relayData);
    }

    /**
     * Adds a relay event to the queue for the specified relay ID.
     *
     * @param relayId the ID of the relay
     * @param event   the event to add
     */
    public void addRelayEvent(int relayId, String event) {
        Deque<String> relayEventQueue = relayEventMap.computeIfAbsent(relayId, k -> new LinkedList<>());
        queueManager.addEvent(relayEventQueue, event);
    }

    /**
     * Retrieves the list of events for the specified relay ID.
     *
     * @param relayId the ID of the relay
     * @return a list of events for the relay
     */
    public List<String> getRelayEvents(int relayId) {
        return new ArrayList<>(relayEventMap.getOrDefault(relayId, new LinkedList<>()));
    }

    /**
     * Retrieves the list of data for the specified relay ID.
     *
     * @param relayId the ID of the relay
     * @return a list of data for the relay
     */
    public List<RelayData> getRelayData(int relayId) {
        return new ArrayList<>(relayDataMap.getOrDefault(relayId, new LinkedList<>()));
    }

    /**
     * Retrieves the list of control ports from the relay data map.
     *
     * @return a list of control ports
     */
    public List<Integer> getControlPorts() {
        return new ArrayList<>(relayDataMap.keySet());
    }
}