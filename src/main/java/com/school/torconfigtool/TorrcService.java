package com.school.torconfigtool;

import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class TorrcService {

    public Path buildTorrcFilePath(String relayNickname, String relayType) {
        return Paths.get(System.getProperty("user.dir"), "torrc", "torrc-" + relayNickname + "_" + relayType);
    }
}
