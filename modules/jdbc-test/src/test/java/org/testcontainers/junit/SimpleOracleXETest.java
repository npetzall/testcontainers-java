package org.testcontainers.junit;

import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.OracleContainer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;

public class SimpleOracleXETest {

    @ClassRule
    public static OracleContainer oracleContainer = new OracleContainer<>()
            .withDrivers("../../ext-lib/ojdbc*");

    @Test
    public void simpleTest() throws SQLException {
        Connection con = oracleContainer.createConnection("");
        Statement statement = con.createStatement();
        statement.execute("SELECT 1 FROM DUAL");
        ResultSet resultSet = statement.getResultSet();

        resultSet.next();
        int resultSetInt = resultSet.getInt(1);
        assertEquals("Test connection query succeeded", 1, resultSetInt);
    }
}
