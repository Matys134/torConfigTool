package com.school.torconfigtool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TorConfigToolApplication {

	public static void main(String[] args) {
		//change port to 8081
		System.getProperties().put( "server.port", 8081 );
		SpringApplication.run(TorConfigToolApplication.class, args);
	}

}
