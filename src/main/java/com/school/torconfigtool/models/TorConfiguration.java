package com.school.torconfigtool.models;

import lombok.Getter;

public class TorConfiguration {
    private String nickname;
    private String orPort;
    private String contact;
    private String hiddenServiceDir;
    private String hiddenServicePort;
    @Getter
    private String controlPort;
    @Getter
    private String socksPort;
    private String bandwidthRate;
    private String bridgeTransportListenAddr;
    private String relayType;

    public TorConfiguration() {
    }

    public void setControlPort(String controlPort) {
        this.controlPort = controlPort;
    }

    public void setSocksPort(String socksPort) {
        this.socksPort = socksPort;
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

    public void setBandwidthRate(String bandwidthRate) {
        this.bandwidthRate = bandwidthRate;
    }

    public void setBridgeTransportListenAddr(String serverTransportListenAddrObfs4) {
        this.bridgeTransportListenAddr = serverTransportListenAddrObfs4;
    }

    public void setRelayType(String relayType) {
        this.relayType = relayType;
    }
}
