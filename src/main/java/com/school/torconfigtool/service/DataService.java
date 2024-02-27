package com.school.torconfigtool.service;

import com.school.torconfigtool.model.RelayData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

/**
 * This is a service class that handles operations related to relay data and events.
 */
@Service
public class DataService {

    // Maximum size for the relay data queue
    private static final int MAX_DATA_SIZE = 50;
    // Maximum size for the relay event queue
    private static final int MAX_EVENT_SIZE = 10;
    private static final Logger logger = LoggerFactory.getLogger(DataService.class);

    /**
     * Method to add relay data to the queue. If the queue is full, it removes the oldest data entry.
     * @param relayDataQueue The queue to which the relay data is to be added.
     * @param relayData The relay data to be added.
     */
    public void addRelayData(Deque<RelayData> relayDataQueue, RelayData relayData) {
        if (relayDataQueue.size() >= MAX_DATA_SIZE) {
            relayDataQueue.poll(); // Remove the oldest data entry
        }
        if (relayData != null) {
            relayDataQueue.offer(relayData); // Add the new data entry only if it's not null
        }
    }

    /**
     * Method to add a relay event to the queue. If the queue is full, it removes the oldest event.
     * @param relayEventQueue The queue to which the event is to be added.
     * @param event The event to be added.
     */
    public void addRelayEvent(Deque<String> relayEventQueue, String event) {
        if (relayEventQueue.size() >= MAX_EVENT_SIZE) {
            relayEventQueue.poll(); // Remove the oldest event
        }
        relayEventQueue.offer(event); // Add the new event
    }

    /**
     * Method to handle relay data. It gets or creates a queue for the relay ID and adds the relay data to it.
     * @param relayId The ID of the relay.
     * @param relayData The relay data to be handled.
     * @param relayDataMap The map of relay data queues.
     */
    public void handleRelayData(int relayId, RelayData relayData, Map<Integer, Deque<RelayData>> relayDataMap) {
        Deque<RelayData> relayDataQueue = relayDataMap.computeIfAbsent(relayId, k -> new LinkedList<>());
        addRelayData(relayDataQueue, relayData);

        logger.info("Relay data for control port {} added. Current data: {}", relayId, relayDataMap);
    }

    /**
     * Method to handle relay events. It gets or creates a queue for the relay ID and adds the event to it.
     * It also adds the event to the relay data if it exists.
     * @param relayId The ID of the relay.
     * @param eventData The event data to be handled.
     * @param relayDataMap The map of relay data queues.
     * @param relayEventMap The map of relay event queues.
     */
    public void handleRelayEvent(int relayId, Map<String, String> eventData, Map<Integer, Deque<RelayData>> relayDataMap, Map<Integer, Deque<String>> relayEventMap) {
        String event = eventData.get("event");

        // Add the event to the relay data
        Deque<RelayData> relayDataQueue = relayDataMap.get(relayId);
        if (relayDataQueue != null) {
            RelayData relayData = new RelayData();
            relayData.setEvent(event);
            addRelayData(relayDataQueue, relayData);
        }

        // Add the event to the relay events
        Deque<String> relayEventQueue = relayEventMap.computeIfAbsent(relayId, k -> new LinkedList<>());
        addRelayEvent(relayEventQueue, event);
    }
}