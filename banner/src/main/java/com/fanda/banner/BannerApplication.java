package com.fanda.banner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.fanda.banner.repository")
public class BannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BannerApplication.class, args);
	}

}
