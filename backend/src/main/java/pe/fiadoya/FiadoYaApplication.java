package pe.fiadoya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FiadoYaApplication {
  public static void main(String[] args) {
    SpringApplication.run(FiadoYaApplication.class, args);
  }
}
