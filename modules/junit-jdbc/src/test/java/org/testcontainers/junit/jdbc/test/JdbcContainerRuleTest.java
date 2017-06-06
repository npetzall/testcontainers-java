package org.testcontainers.junit.jdbc.test;

import org.hamcrest.CoreMatchers;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jdbc.JdbcContainerRule;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.rnorth.visibleassertions.VisibleAssertions.assertThat;
import static org.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;

public class JdbcContainerRuleTest {

    @ClassRule
    public static JdbcContainerRule<MySQLContainer> jdbcContainerRule = new JdbcContainerRule<>(() -> new MySQLContainer())
            .withInitScript("org/testcontainers/junit/jdbc/initscripts/mysqlInitScript.sql")
            .assumeDockerIsPresent()
            .withAssumptions(assumeDriverIsPresent())
            .withInitFunctions(connection -> {
                try {
                    Statement statement = connection.createStatement();
                    statement.execute("INSERT INTO junit_jdbc values (2, 'added by function');");
                    if (!connection.getAutoCommit()) {
                        connection.commit();
                    }
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

    @Test
    public void scriptExecuted() throws SQLException {
        String text = getText(1);
        assertThat("Found text added by script", text, CoreMatchers.is("added by script"));
    }

    @Test
    public void functionExecuted() throws SQLException {
        String text = getText(2);
        assertThat("Found text added by function", text, CoreMatchers.is("added by function"));
    }

    private String getText(int id) throws SQLException {
        Connection connection = jdbcContainerRule.getContainer().createConnection("");
        ResultSet rs = connection.createStatement().executeQuery("SELECT text FROM junit_jdbc where ID="+id);
        rs.next();
        return rs.getString(1);
    }
}
