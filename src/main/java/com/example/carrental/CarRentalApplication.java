package com.example.carrental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan("com.example.carrental.model")

@SpringBootApplication
public class CarRentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarRentalApplication.class, args);
    }

}
