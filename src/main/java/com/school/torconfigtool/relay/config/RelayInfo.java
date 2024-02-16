package com.school.torconfigtool.relay.config;

/**
 * This is a record class named RelayInfo. A record in Java is a compact way of declaring a class.
 * This class is immutable and has a clear purpose, which is to hold information about a relay.
 *
 *
 * @param controlPort The control port number for the relay.
 * @param nickname The nickname for the relay.
 * @param type The type of the relay.
 */

public record RelayInfo(int controlPort, String nickname, String type) {

}