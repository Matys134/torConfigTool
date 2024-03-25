package com.school.torconfigtool.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Service
public class Obfs4Service {

    public String getObfs4Link(String relayNickname) {
        String dataDirectoryPath = System.getProperty("user.dir") + File.separator + "torrc" + File.separator + "dataDirectory";
        String obfs4FilePath = dataDirectoryPath + File.separator + relayNickname + "_BridgeConfig" + File.separator + "pt_state" + File.separator + "obfs4_bridgeline.txt";

        System.out.println("Obfs4 file path: " + obfs4FilePath);

        String obfs4Link = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(obfs4FilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Bridge obfs4")) {
                    obfs4Link = line;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read obfs4_bridgeline.txt file", e);
        }

        return obfs4Link;
    }
}
