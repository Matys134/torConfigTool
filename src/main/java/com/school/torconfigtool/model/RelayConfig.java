// RelayConfig.java
package com.school.torconfigtool.model;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * This interface defines the methods for managing and writing the configuration of a Tor relay.
 */
public interface RelayConfig {

    /**
     * Gets the nickname of the Tor relay.
     *
     * @return The nickname of the Tor relay.
     */
    String getNickname();

    /**
     * Sets the nickname of the Tor relay.
     *
     * @param nickname The nickname to be set for the Tor relay.
     */
    void setNickname(String nickname);

    /**
     * Gets the OR (Onion Router) port of the Tor relay.
     *
     * @return The OR port of the Tor relay.
     */
    String getOrPort();

    /**
     * Sets the OR (Onion Router) port of the Tor relay.
     *
     * @param orPort The OR port to be set for the Tor relay.
     */
    void setOrPort(String orPort);

    /**
     * Gets the contact information of the Tor relay operator.
     *
     * @return The contact information of the Tor relay operator.
     */
    String getContact();

    /**
     * Sets the contact information of the Tor relay operator.
     *
     * @param contact The contact information to be set for the Tor relay operator.
     */
    void setContact(String contact);

    /**
     * Gets the control port of the Tor relay.
     *
     * @return The control port of the Tor relay.
     */
    String getControlPort();

    /**
     * Sets the control port of the Tor relay.
     *
     * @param controlPort The control port to be set for the Tor relay.
     */
    void setControlPort(String controlPort);

    /**
     * Writes the specific configuration of the Tor relay to a BufferedWriter.
     *
     * @param writer The BufferedWriter to which the configuration should be written.
     * @throws IOException If an I/O error occurs.
     */
    void writeSpecificConfig(BufferedWriter writer) throws IOException;

    // get and set methods for bandwidthRate
    String getBandwidthRate();
    void setBandwidthRate(String bandwidthRate);
}