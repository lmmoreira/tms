package br.com.logistics.tms;

import org.springframework.boot.SpringApplication;

public class TestTmsApplication {

	public static void main(String[] args) {
		SpringApplication.from(TmsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
