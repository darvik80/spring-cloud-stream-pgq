package xyz.crearts.stream.pgq.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "xyz.crearts.stream.pgq")
@EnableScheduling
public class SpringCloudStreamPgqApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringCloudStreamPgqApplication.class, args);
    }

}
