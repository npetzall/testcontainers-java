package org.testcontainers.junit;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.util.PropertyElf;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.utility.JdbcDriverUtil;

import javax.sql.DataSource;
import java.net.URLClassLoader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;

/**
 * @author gusohal
 */
public class SimpleOracleTest {

    @Rule
    public OracleContainer oracle = new OracleContainer<>().withDrivers("../../ext-lib/ojdbc*");

    @Test
    public void testSimple() throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        HikariConfig hikariConfig = new HikariConfig();
        DataSource dataSource = getDataSource();

        hikariConfig.setDataSource(dataSource);
        hikariConfig.setUsername(oracle.getUsername());
        hikariConfig.setPassword(oracle.getPassword());

        HikariDataSource ds = new HikariDataSource(hikariConfig);
        Statement statement = ds.getConnection().createStatement();
        statement.execute("SELECT 1 FROM dual");
        ResultSet resultSet = statement.getResultSet();

        resultSet.next();
        int resultSetInt = resultSet.getInt(1);
        assertEquals("A basic SELECT query succeeds", 1, resultSetInt);
    }

    private DataSource getDataSource() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        URLClassLoader urlClassLoader = URLClassLoader.newInstance(JdbcDriverUtil.getDrivers("../../ext-lib/ojdbc*"), this.getClass().getClassLoader());
        DataSource dataSource = (DataSource) Class.forName("oracle.jdbc.pool.OracleDataSource",true, urlClassLoader).newInstance();
        Properties properties = new Properties();
        properties.put("URL", oracle.getJdbcUrl());
        PropertyElf.setTargetFromProperties(dataSource, properties);
        return dataSource;
    }
}
