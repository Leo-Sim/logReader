package com.leo.log.creator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class LogReaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(LogReaderApplication.class, args);
	}

}
