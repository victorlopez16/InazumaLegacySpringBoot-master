package net.elpuig.inazumalegacy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InazumaLegacyApplication {

	public static void main(String[] args) {
		System.setProperty("java.net.preferIPv4Stack" , "true");

		SpringApplication.run(InazumaLegacyApplication.class, args);
	}

}