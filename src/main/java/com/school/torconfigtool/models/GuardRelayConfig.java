package com.school.torconfigtool.models;

public class GuardRelayConfig {
    private String nickname;
    private String orPort;
    private String contact;
    private String controlPort;
    private String socksPort;
    // Add other attributes here as needed

    // Getters and setters for the attributes

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getOrPort() {
        return orPort;
    }

    public void setOrPort(String orPort) {
        this.orPort = orPort;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getControlPort() {
        return controlPort;
    }

    public void setControlPort(String controlPort) {
        this.controlPort = controlPort;
    }

    public String getSocksPort() {
        return socksPort;
    }

    public void setSocksPort(String socksPort) {
        this.socksPort = socksPort;
    }
}
