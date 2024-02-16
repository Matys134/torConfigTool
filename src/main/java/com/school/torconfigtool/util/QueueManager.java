package com.school.torconfigtool.util;

import com.school.torconfigtool.relay.data.RelayData;
import java.util.Deque;

/**
 * The QueueManager class is responsible for managing the data and event queues.
 * It ensures that the size of the queues does not exceed their maximum sizes.
 * If a queue is full, it removes the oldest entry before adding a new one.
 */
public class QueueManager {
    // Maximum size of the data queue
    private static final int MAX_DATA_SIZE = 50;
    // Maximum size of the event queue
    private static final int MAX_EVENT_SIZE = 10;

    /**
     * Adds a new data entry to the relay data queue.
     * If the queue is full, it removes the oldest entry before adding the new one.
     * If the new data entry is null, it does not add it to the queue.
     *
     * @param relayDataQueue The queue to which the data entry is to be added
     * @param relayData The data entry to be added to the queue
     */
    public void addData(Deque<RelayData> relayDataQueue, RelayData relayData) {
        if (relayDataQueue.size() >= MAX_DATA_SIZE) {
            relayDataQueue.poll(); // Remove the oldest data entry
        }
        if (relayData != null) {
            relayDataQueue.offer(relayData); // Add the new data entry only if it's not null
        }
    }

    /**
     * Adds a new event to the relay event queue.
     * If the queue is full, it removes the oldest entry before adding the new one.
     *
     * @param relayEventQueue The queue to which the event is to be added
     * @param event The event to be added to the queue
     */
    public void addEvent(Deque<String> relayEventQueue, String event) {
        if (relayEventQueue.size() >= MAX_EVENT_SIZE) {
            relayEventQueue.poll(); // Remove the oldest event
        }
        relayEventQueue.offer(event); // Add the new event
    }
}