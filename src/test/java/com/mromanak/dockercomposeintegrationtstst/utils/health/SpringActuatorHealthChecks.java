package com.mromanak.dockercomposeintegrationtstst.utils.health;

import com.palantir.docker.compose.connection.Container;
import com.palantir.docker.compose.connection.waiting.HealthCheck;
import com.palantir.docker.compose.connection.waiting.SuccessOrFailure;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class SpringActuatorHealthChecks {

    public static HealthCheck<Container> healthCheckIsUp(int internalPort) {
        return healthCheckIsUp(internalPort, new RestTemplate());
    }

    public static HealthCheck<Container> healthCheckIsUp(int internalPort, RestTemplate restTemplate) {
        Objects.requireNonNull(restTemplate, "restTemplate must be non-null");

        return (Container container) -> {
            try {
                String uriString = container.port(internalPort).inFormat("http://$HOST:$EXTERNAL_PORT/actuator/health");
                RequestEntity<Void> request = RequestEntity.get(URI.create(uriString)).build();
                ParameterizedTypeReference<Map<String, String>> typeReference = new ParameterizedTypeReference<>() {};
                ResponseEntity<Map<String, String>> response = restTemplate.exchange(request, typeReference);

                if (response.getStatusCode() != HttpStatus.OK) {
                    return SuccessOrFailure.failure("Health check failed with HTTP status " + response.getStatusCode());
                }

                Map<String, String> responseBody = response.getBody();
                if (responseBody == null) {
                    return SuccessOrFailure.failure("Health check failed with empty body");
                } else if (!Objects.equals(responseBody.get("status"), "UP")) {
                    return SuccessOrFailure.failure("Health check failed with status message " +
                            responseBody.get("status"));
                }

                return SuccessOrFailure.success();
            } catch (RuntimeException e) {
                return SuccessOrFailure.failureWithCondensedException("Health check failed with runtime exception", e);
            }
        };
    }
}
