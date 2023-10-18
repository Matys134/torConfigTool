package com.school.torconfigtool.models;

public class TorConfiguration {
    private String nickname;
    private String orPort;
    private String contact;
    private String hiddenServiceDir;
    private String hiddenServicePort;

    public TorConfiguration() {
    }

    public TorConfiguration(String nickname, String orPort, String contact, String hiddenServiceDir, String hiddenServicePort) {
        this.nickname = nickname;
        this.orPort = orPort;
        this.contact = contact;
        this.hiddenServiceDir = hiddenServiceDir;
        this.hiddenServicePort = hiddenServicePort;
    }

    public String getNickname() {
        return nickname;
    }

    public String getOrPort() {
        return orPort;
    }

    public String getContact() {
        return contact;
    }

    public String getHiddenServiceDir() {
        return hiddenServiceDir;
    }

    public String getHiddenServicePort() {
        return hiddenServicePort;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setOrPort(String orPort) {
        this.orPort = orPort;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setHiddenServiceDir(String hiddenServiceDir) {
        this.hiddenServiceDir = hiddenServiceDir;
    }

    public void setHiddenServicePort(String hiddenServicePort) {
        this.hiddenServicePort = hiddenServicePort;
    }
}
