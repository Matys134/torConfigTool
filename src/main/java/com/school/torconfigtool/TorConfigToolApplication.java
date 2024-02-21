package com.school.torconfigtool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@SpringBootApplication
public class TorConfigToolApplication {

    public static void main(String[] args) {
        //change port to 8081
        System.getProperties().put("server.port", 8080);

        SpringApplication.run(TorConfigToolApplication.class, args);

        // Call the Python script
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "/path/to/__init__.py");
            Process p = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String ret = in.readLine();
            System.out.println("Python script output: " + ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}