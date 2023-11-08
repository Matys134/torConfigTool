package com.school.torconfigtool.models;

import lombok.Data;

@Data
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
}
