package com.school.torconfigtool.models;

public class RelayInfo {
    private int controlPort;
    private String nickname;
    private String type; // Add this line

    public RelayInfo(int controlPort, String nickname, String type) { // Modify this line
        this.controlPort = controlPort;
        this.nickname = nickname;
        this.type = type; // Add this line
    }

    public int getControlPort() {
        return controlPort;
    }

    public String getNickname() {
        return nickname;
    }

    public String getType() { // Add this method
        return type;
    }
}