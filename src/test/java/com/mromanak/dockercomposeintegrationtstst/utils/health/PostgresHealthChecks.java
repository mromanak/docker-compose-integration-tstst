package com.mromanak.dockercomposeintegrationtstst.utils.health;

import com.palantir.docker.compose.connection.Container;
import com.palantir.docker.compose.connection.waiting.HealthCheck;
import com.palantir.docker.compose.connection.waiting.SuccessOrFailure;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgresHealthChecks {

    public static HealthCheck<Container> canConnectTo(int internalPort, String database, String username, String password) {
        return (Container container) -> {
            try (Connection connection = DriverManager.getConnection(
                    container.port(internalPort).inFormat("jdbc:postgresql://$HOST:$EXTERNAL_PORT/" + database),
                    username, password
            )) {
                return SuccessOrFailure.success();
            } catch (SQLException e) {
                return SuccessOrFailure.failureWithCondensedException("Failed to connect", e);
            }
        };
    }
}
