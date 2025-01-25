package com.cloud.demo.config;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import java.util.List;

@Configuration
public class LoadBalancerConfig {

    @Bean
    public ServiceInstanceListSupplier serviceInstanceListSupplier() {
        return new SimpleServiceInstanceListSupplier("appointments-service");
    }

    private static class SimpleServiceInstanceListSupplier implements ServiceInstanceListSupplier {
        private final String serviceId;

        public SimpleServiceInstanceListSupplier(String serviceId) {
            this.serviceId = serviceId;
        }

        @Override
        public String getServiceId() {
            return serviceId;
        }

        @Override
        public Flux<List<ServiceInstance>> get() { // Changed return type
            return Flux.just(List.of(
                    new DefaultServiceInstance(
                            serviceId + "-1",
                            serviceId,
                            System.getenv("APPOINTMENTS_SERVER_1_HOST"),
                            Integer.parseInt(System.getenv("APPOINTMENTS_SERVER_1_PORT")),
                            false),
                    new DefaultServiceInstance(
                            serviceId + "-2",
                            serviceId,
                            System.getenv("APPOINTMENTS_SERVER_2_HOST"),
                            Integer.parseInt(System.getenv("APPOINTMENTS_SERVER_2_PORT")),
                            false)));
        }
    }
}