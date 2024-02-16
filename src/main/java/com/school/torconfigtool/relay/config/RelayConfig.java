// RelayConfig.java
package com.school.torconfigtool.relay.config;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * The RelayConfig interface provides a contract for classes that represent a relay configuration.
 * It includes methods for getting and setting the nickname, OR port, contact, and control port.
 * It also includes a method for writing the specific configuration to a BufferedWriter.
 */
public interface RelayConfig {

    /**
     * Gets the nickname of the relay.
     * @return the nickname of the relay.
     */
    String getNickname();

    /**
     * Sets the nickname of the relay.
     * @param nickname the new nickname of the relay.
     */
    void setNickname(String nickname);

    /**
     * Gets the OR port of the relay.
     * @return the OR port of the relay.
     */
    String getOrPort();

    /**
     * Sets the OR port of the relay.
     * @param orPort the new OR port of the relay.
     */
    void setOrPort(String orPort);

    /**
     * Gets the contact information of the relay.
     * @return the contact information of the relay.
     */
    String getContact();

    /**
     * Sets the contact information of the relay.
     * @param contact the new contact information of the relay.
     */
    void setContact(String contact);

    /**
     * Gets the control port of the relay.
     * @return the control port of the relay.
     */
    String getControlPort();

    /**
     * Sets the control port of the relay.
     * @param controlPort the new control port of the relay.
     */
    void setControlPort(String controlPort);

    /**
     * Writes the specific configuration to a BufferedWriter.
     * @param writer the BufferedWriter to write the configuration to.
     * @throws IOException if an I/O error occurs.
     */
    void writeSpecificConfig(BufferedWriter writer) throws IOException;
}