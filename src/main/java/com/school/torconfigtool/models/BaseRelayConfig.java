package com.school.torconfigtool.models;

import lombok.Data;

@Data
public class BaseRelayConfig {
    private String nickname;
    private String orPort;
    private String contact;
    private String controlPort;
    private String socksPort;
    // Add other common attributes and methods here...
}
