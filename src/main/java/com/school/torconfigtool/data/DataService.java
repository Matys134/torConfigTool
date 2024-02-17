package com.school.torconfigtool.data;

import com.school.torconfigtool.relay.RelayData;
import org.springframework.stereotype.Service;

import java.util.Deque;

/**
 * This is a service class that handles operations related to relay data and events.
 */
@Service
public class DataService {

    // Maximum size for the relay data queue
    private static final int MAX_DATA_SIZE = 50;
    // Maximum size for the relay event queue
    private static final int MAX_EVENT_SIZE = 10;

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
}