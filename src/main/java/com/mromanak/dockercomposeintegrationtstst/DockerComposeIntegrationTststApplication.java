package com.mromanak.dockercomposeintegrationtstst;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.mromanak"})
@EnableJpaRepositories(basePackages = {"com.mromanak.dockercomposeintegrationtstst.repository"})
@EntityScan(basePackages = {"com.mromanak.dockercomposeintegrationtstst.model.jpa"})
public class DockerComposeIntegrationTststApplication {

    public static void main(String[] args) {
        SpringApplication.run(DockerComposeIntegrationTststApplication.class, args);
    }

}
