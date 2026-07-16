package vn.edu.hcmuaf.fit.quanlythuchi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.cert.X509Certificate;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class QuanlythuchiApplication {
	public static void main(String[] args) {
		SpringApplication.run(QuanlythuchiApplication.class, args);
	}

}
