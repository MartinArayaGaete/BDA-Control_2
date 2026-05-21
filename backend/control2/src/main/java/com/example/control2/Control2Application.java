package com.example.control2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Control2Application {

	public static void main(String[] args) {
		SpringApplication.run(Control2Application.class, args);
	}

}
