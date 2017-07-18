package bmrobin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author brobinson
 */
@SpringBootApplication
public class MyApplication {

    public static void main(String ... args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
