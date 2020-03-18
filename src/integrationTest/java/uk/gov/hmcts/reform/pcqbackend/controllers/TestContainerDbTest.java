package uk.gov.hmcts.reform.pcqbackend.controllers;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.jdbc.ContainerDatabaseDriver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@SpringBootTest
public class TestContainerDbTest {

    @ClassRule
    public static PostgreSQLContainer postgresContainer = new PostgreSQLContainer<>();

    @BeforeClass
    @SuppressWarnings({"PMD.JUnit4TestShouldUseBeforeAnnotation"})
    public static void setUp() {
        String jdbcUrl = postgresContainer.getJdbcUrl();
        String username = postgresContainer.getUsername();
        String password = postgresContainer.getPassword();
        Flyway flyway = Flyway.configure().dataSource(
            jdbcUrl, username, password).load();
        flyway.migrate();
    }

    @Test
    @SuppressWarnings({"PMD.CloseResource"})
    public void testDatabaseExists() throws SQLException {
        String jdbcUrl = postgresContainer.getJdbcUrl();
        String username = postgresContainer.getUsername();
        String password = postgresContainer.getPassword();
        Connection conn = DriverManager
                .getConnection(jdbcUrl, username, password);
        ResultSet resultSet =
            conn.createStatement().executeQuery("SELECT count(*) from protected_characteristics");
        if (resultSet.next()) {    // result is properly examined and used
            assertEquals("Does protected_characteristics table exist",0, resultSet.getInt(1));
        }
        JdbcUtils.closeConnection(conn);
        JdbcUtils.closeResultSet(resultSet);
    }

    @Test
    @AfterClass
    @SuppressWarnings({"PMD.JUnit4TestShouldUseAfterAnnotation"})
    public static void testCleanup() {
        postgresContainer.stop();
        ContainerDatabaseDriver.killContainers();
        assertFalse("Ensure container is stopped.", postgresContainer.isRunning());
    }
}
