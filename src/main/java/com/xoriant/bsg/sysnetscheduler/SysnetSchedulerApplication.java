package com.xoriant.bsg.sysnetscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SysnetSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SysnetSchedulerApplication.class, args);
	}

}
