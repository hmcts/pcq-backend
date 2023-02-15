package uk.gov.hmcts.reform.pcqbackend.controllers;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

@SpringBootTest
public class TestContainerDbTest {

    public static final String DOCKER_IMAGE_PG_11_ALPINE = "postgres:11-alpine";

    @ClassRule
    public static final PostgreSQLContainer POSTGRE_SQL_CONTAINER
        = new PostgreSQLContainer<>(DOCKER_IMAGE_PG_11_ALPINE);

    @BeforeClass
    @SuppressWarnings({"PMD.JUnit4TestShouldUseBeforeAnnotation"})
    public static void setUp() {
        String jdbcUrl = POSTGRE_SQL_CONTAINER.getJdbcUrl();
        String username = POSTGRE_SQL_CONTAINER.getUsername();
        String password = POSTGRE_SQL_CONTAINER.getPassword();
        Flyway flyway = Flyway.configure().dataSource(
            jdbcUrl, username, password).load();
        flyway.migrate();
    }

    @Test
    @SuppressWarnings({"PMD.CloseResource"})
    public void testDatabaseExists() throws SQLException {
        String jdbcUrl = POSTGRE_SQL_CONTAINER.getJdbcUrl();
        String username = POSTGRE_SQL_CONTAINER.getUsername();
        String password = POSTGRE_SQL_CONTAINER.getPassword();
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
    @SuppressWarnings({"PMD.CloseResource"})
    public void testAnotherDatabaseExists() throws SQLException {
        String jdbcUrl = POSTGRE_SQL_CONTAINER.getJdbcUrl();
        String username = POSTGRE_SQL_CONTAINER.getUsername();
        String password = POSTGRE_SQL_CONTAINER.getPassword();
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
}
