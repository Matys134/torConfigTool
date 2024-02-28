package com.school.torconfigtool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class TorConfigToolApplication {

    public static void main(String[] args) {

        Path torrcPath = Paths.get("torrc", "dataDirectory");
        if (!Files.exists(torrcPath)) {
            try {
                // Create the directory
                Files.createDirectories(torrcPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // create the directory for onion
        Path onionPath = Paths.get("onion", "hiddenServiceDirs");
        if (!Files.exists(onionPath)) {
            try {
                // Create the directory
                Files.createDirectories(onionPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("python3");

        try {
            // Execute the Python script
            engine.eval(new FileReader("src/main/java/com/school/torconfigtool/python/data.py"));
        } catch (Exception e) {
            e.printStackTrace();
        }


        System.getProperties().put("server.port", 8080);

        SpringApplication.run(TorConfigToolApplication.class, args);
    }
}