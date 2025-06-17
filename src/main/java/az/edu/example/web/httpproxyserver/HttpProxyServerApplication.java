package az.edu.example.web.httpproxyserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class HttpProxyServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HttpProxyServerApplication.class, args);
    }

}
