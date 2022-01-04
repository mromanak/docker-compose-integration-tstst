package com.mromanak.dockercomposeintegrationtstst.controller;

import com.mromanak.dockercomposeintegrationtstst.model.dto.ItemResponseDto;
import com.mromanak.dockercomposeintegrationtstst.utils.health.PostgresHealthChecks;
import com.mromanak.dockercomposeintegrationtstst.utils.health.SpringActuatorHealthChecks;
import com.mromanak.dockercomposeintegrationtstst.utils.junit.DockerIntegrationTest;
import com.mromanak.dockercomposeintegrationtstst.utils.test.RestApiTest;
import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.DockerMachine;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

@Category(value = DockerIntegrationTest.class)
public class ItemControllerDockerIntegrationTest extends RestApiTest {

    private static final String POSTGRES_DB = "integrationtests";
    private static final String POSTGRES_USER = "postgres";
    private static final String POSTGRES_PASSWORD = "16i3hJFvIR7d";

    private final DockerMachine LOCAL_DOCKER_MACHINE = DockerMachine.localMachine().
            withAdditionalEnvironmentVariable("POSTGRES_HOST", "host.docker.internal").
            withAdditionalEnvironmentVariable("POSTGRES_PORT", "5432").
            withAdditionalEnvironmentVariable("POSTGRES_DB", POSTGRES_DB).
            withAdditionalEnvironmentVariable("POSTGRES_USER", POSTGRES_USER).
            withAdditionalEnvironmentVariable("POSTGRES_PASSWORD", POSTGRES_PASSWORD).
            withAdditionalEnvironmentVariable("DDL_MODE", "create-drop").
            build();

    @Rule
    private final DockerComposeRule DOCKER_COMPOSE_RULE = DockerComposeRule.builder().
            file("src/test/resources/dockerIntegrationTestStack.yml").
            machine(LOCAL_DOCKER_MACHINE).
            waitingForService("db", HealthChecks.toHaveAllPortsOpen()).
            waitingForService("db", PostgresHealthChecks.canConnectTo(5432, POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD)).
            waitingForService("api", HealthChecks.toHaveAllPortsOpen()).
            waitingForService("api", SpringActuatorHealthChecks.healthCheckIsUp(8080)).
            build();

    @Test
    public void item123DoesNotExist_getRequestIsMadeForItem123_thenRespondWithHttpStatus404AndEmptyBody() {
        URI uri = URI.create("http://localhost:8080/repository/item/1");
        ParameterizedTypeReference<ItemResponseDto> type = new ParameterizedTypeReference<>() {
        };
        HttpClientErrorException.NotFound exception = assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> makeGetRequest(uri, type)
        );

        assertThat(exception.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(exception.getResponseBodyAsString(), is(""));
    }
}