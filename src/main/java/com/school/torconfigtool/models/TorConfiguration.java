package com.school.torconfigtool.models;

import lombok.Getter;

@Getter
public class TorConfiguration {
    private String nickname;
    private String orPort;
    private String contact;
    private String hiddenServiceDir;
    private String hiddenServicePort;
    private String controlPort;
    private String socksPort;
    private String bandwidthRate;
    private String bridgeTransportListenAddr;
    private String relayType;
    private String hostname;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public TorConfiguration() {
    }

    public void setControlPort(String controlPort) {
        this.controlPort = controlPort;
    }

    public void setSocksPort(String socksPort) {
        this.socksPort = socksPort;
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

    public void setBridgeTransportListenAddr(String bridgeTransportListenAddr) {
        this.bridgeTransportListenAddr = bridgeTransportListenAddr;
    }

    public void setRelayType(String relayType) {
        this.relayType = relayType;
    }
}
