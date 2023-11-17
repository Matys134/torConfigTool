package com.school.torconfigtool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TorConfigToolApplication {

	public static void main(String[] args) {
		//change port to 8081
		System.getProperties().put( "server.port", 8081 );

		checkRequiredPrograms();

		SpringApplication.run(TorConfigToolApplication.class, args);
	}

	private static void checkRequiredPrograms() {
		String[] requiredPrograms = {"nginx", "obfs4proxy", "tor"};

		for (String program : requiredPrograms) {
			try {
				Process process = Runtime.getRuntime().exec("which " + program);
				process.waitFor();
				if (process.exitValue() != 0) {
					System.out.println("Required program " + program + " not found. Exiting...");
				}
				else {
					System.out.println("Required program " + program + " found.");
				}
			} catch (Exception e) {
				System.out.println("Required program " + program + " not found. Exiting...");
			}
		}
	}
}
