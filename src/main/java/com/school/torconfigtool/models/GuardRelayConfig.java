package com.school.torconfigtool.models;

import lombok.Data;

@Data
public class GuardRelayConfig {
    private String nickname;
    private String orPort;
    private String contact;
    private String controlPort;
    private String socksPort;
    // Add other attributes here as needed
}
