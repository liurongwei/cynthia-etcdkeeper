package com.cydia.etcdkeeper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableAutoConfiguration
@EnableCaching
public class EtcdkeeperApplication {

    public static void main(String[] args) {
        SpringApplication.run(EtcdkeeperApplication.class, args);
    }

}
