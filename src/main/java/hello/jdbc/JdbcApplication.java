package hello.jdbc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class JdbcApplication {

	public static void main(String[] args) {SpringApplication.run(JdbcApplication.class, args);

	}
}
