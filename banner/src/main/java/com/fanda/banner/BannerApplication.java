package com.fanda.banner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients(basePackages = "com.fanda.banner.repository")
public class BannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BannerApplication.class, args);
	}

}
