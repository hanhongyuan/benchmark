package com.benchmark.gateway;


import com.netflix.appinfo.InstanceInfo;
import javafx.application.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurerComposite;

import java.util.List;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;

@RestController
@EnableDiscoveryClient
@SpringBootApplication
public class GatewayApplication {

    @Bean
    public DiscoveryClientRouteDefinitionLocator
    discoveryClientRouteLocator(DiscoveryClient discoveryClient) {

        return new DiscoveryClientRouteDefinitionLocator(discoveryClient);
    }

    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/service")
    public List<ServiceInstance> serviceInstances() {
        List<String> services = discoveryClient.getServices();
        String description = discoveryClient.description();
        System.out.println("services: " + services);
        System.out.println("Description: " + description);
        for (String service : services) {
            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            System.out.println("Instances: " + instances);
            for (ServiceInstance instance : instances) {
                System.out.println("Service:  " + instance.getServiceId());
                System.out.println("Host:     " + instance.getHost());
                System.out.println("Uri:      " + instance.getUri());
                System.out.println("Port:     " + instance.getPort());
                System.out.println("Metadata: " + instance.getMetadata());
            }
        }
        return null;
    }

    @Bean
    public WebFluxConfigurer corsConfigurer() {
        return new WebFluxConfigurerComposite() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("*")
                        .allowedMethods("*");
            }
        };
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/get")
                        .filters(f ->
                                f.addResponseHeader("X-TestHeader", "foobar"))
                        .uri("http://httpbin.org:80")
                )
                .route(r -> r.path("/api/upload/**")
                        .filters(f -> f.rewritePath("/api/upload/(?<segment>.*)", "api/upload/${segment}"))
                        .uri("lb://upload-service/api/upload")
                )
                .route(r -> r.path("/api/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<segment>.*)", "api/${segment}"))
//                                .hystrix(fallbackMethod()))
                        .uri("lb://registry-service")
                )
                .route(r -> r.path("/stream/**")
                        .filters(f -> f.rewritePath("/stream/(?<segment>.*)", "stream/${segment}"))
                        .uri("http://localhost:8888")
                )
                .route(r -> r.path("/**")
//                        .filters(f -> {
//                            f.rewritePath("/(?<segment>.*)", "/${segment}");
//                            return f.stripPrefix(0);
//                        })
                        .uri("http://localhost:3000")
                )
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
