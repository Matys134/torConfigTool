package com.school.torconfigtool.model;

/**
 * This is a record class named RelayInfo.
 * A record is a special kind of class in Java that is used to model plain data aggregates.
 * <p>
 * The RelayInfo record has three fields:
 * - controlPort: an integer representing the control port number.
 * - nickname: a String representing the nickname.
 * - type: a String representing the type.
 * <p>
 * The @Getter annotation from the Lombok library is used to automatically generate getter methods for these fields.
 */

public record RelayInfo(int controlPort, String nickname, String type) {
}