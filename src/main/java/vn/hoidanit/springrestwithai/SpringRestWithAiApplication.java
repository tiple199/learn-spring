package vn.hoidanit.springrestwithai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringRestWithAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringRestWithAiApplication.class, args);
	}

}
