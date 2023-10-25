package com.school.torconfigtool.models;

import lombok.Getter;

@Getter
public class TorConfiguration {
    private String nickname;
    private String orPort;
    private String contact;
    private String hiddenServiceDir;
    private String hiddenServicePort;

    public TorConfiguration() {
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
