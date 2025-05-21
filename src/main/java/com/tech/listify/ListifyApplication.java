package com.tech.listify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ListifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ListifyApplication.class, args);
    }

}
