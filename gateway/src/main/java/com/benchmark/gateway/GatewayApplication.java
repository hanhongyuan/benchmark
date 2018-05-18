package com.benchmark.gateway;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.context.annotation.Bean;

@EnableDiscoveryClient
@EnableCircuitBreaker
@SpringBootApplication
public class GatewayApplication {


    @Bean DiscoveryClientRouteDefinitionLocator discoveryRoutes (DiscoveryClient dc) {
        return new DiscoveryClientRouteDefinitionLocator(dc);
    }

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);


    }

}
