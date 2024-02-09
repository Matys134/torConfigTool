package com.school.torconfigtool.models;

public class RelayInfo {
    private int controlPort;
    private String nickname;

    public RelayInfo(int controlPort, String nickname) {
        this.controlPort = controlPort;
        this.nickname = nickname;
    }

    public int getControlPort() {
        return controlPort;
    }

    public String getNickname() {
        return nickname;
    }
}