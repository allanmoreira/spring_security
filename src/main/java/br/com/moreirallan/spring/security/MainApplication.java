package br.com.moreirallan.spring.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Locale;


@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@EnableWebMvc
@ComponentScan({
        "br.com.moreirallan.spring.security"
})
public class MainApplication {

    public static void main(String[] args) {
        Locale.setDefault(new Locale("pt", "BR"));
        SpringApplication springApplication = new SpringApplication(MainApplication.class);
        springApplication.run(args);
    }

}
