package com.roboadvisor.jeonbongjun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class JeonbongjunApplication {

	public static void main(String[] args) {

		SpringApplication.run(JeonbongjunApplication.class, args);
	}

}
