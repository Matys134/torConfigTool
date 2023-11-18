// RelayConfig.java
package com.school.torconfigtool.models;

import com.school.torconfigtool.models.BaseRelayConfig;
import lombok.Data;

import java.io.BufferedWriter;
import java.io.IOException;

public interface RelayConfig {
    String getNickname();
    void setNickname(String nickname);

    String getOrPort();
    void setOrPort(String orPort);

    String getContact();
    void setContact(String contact);

    String getControlPort();
    void setControlPort(String controlPort);

    String getSocksPort();
    void setSocksPort(String socksPort);
    void writeSpecificConfig(BufferedWriter writer) throws IOException;
}
