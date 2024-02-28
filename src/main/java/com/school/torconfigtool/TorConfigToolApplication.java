package com.school.torconfigtool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class TorConfigToolApplication {

    public static void main(String[] args) throws IOException {

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

        try {
            // Specify the path to the Python interpreter and the Python script
            String pythonInterpreterPath = "venv";
            String pythonScriptPath = "src/main/java/com/school/torconfigtool/python/data.py";

            // Create a ProcessBuilder
            ProcessBuilder processBuilder = new ProcessBuilder(pythonInterpreterPath, pythonScriptPath);

            // Start the process
            Process process = processBuilder.start();

            // Get the input stream
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for the process to finish and get the exit value
            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println("Success!");
            } else {
                // Abnormal termination: Log an error, throw an exception, or take other appropriate action
                System.out.println("Python script execution failed with exit code " + exitVal);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }


        System.getProperties().put("server.port", 8080);

        SpringApplication.run(TorConfigToolApplication.class, args);
    }
}