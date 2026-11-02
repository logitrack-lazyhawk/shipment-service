package com.logitrack.shipmentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients
public class ShipmentServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(ShipmentServiceApplication.class, args);
    }

}
