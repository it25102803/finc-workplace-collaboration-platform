package com.example.workplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.workplace", "com.example.demo"})
public class WorkplaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkplaceApplication.class, args);
    }
}
