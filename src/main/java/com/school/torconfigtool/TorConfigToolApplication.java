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
		// Add the names of the programs you want to check for
		String[] requiredPrograms = {"nginx", "obfs4proxy"};

		// Iterate through the list of required programs and check their availability
		for (String program : requiredPrograms) {
			if (!isProgramInstalled(program)) {
				System.err.println("Error: " + program + " is not installed. Please install it before running the application.");
			}
		}
	}

	public static boolean isProgramInstalled(String programName) {
		try {
			// Use the "which" command to check if the program is installed
			Process process = new ProcessBuilder("which", programName).start();
			int exitCode = process.waitFor();

			// If the exit code is 0, the program is installed; otherwise, it's not installed
			return exitCode == 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
