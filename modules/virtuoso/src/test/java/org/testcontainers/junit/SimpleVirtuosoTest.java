package org.testcontainers.junit;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.util.PropertyElf;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.VirtuosoContainer;
import org.testcontainers.utility.JdbcDriverUtil;

import javax.sql.DataSource;
import java.net.URLClassLoader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;

public class SimpleVirtuosoTest {

    @Rule
    public VirtuosoContainer virtuoso = new VirtuosoContainer<>()
            .withDrivers("../../ext-lib/virtjdbc*");

    @Test
    public void testSimple() throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDataSource(getDataSource());
        hikariConfig.setConnectionTestQuery("SELECT 1");

        HikariDataSource ds = new HikariDataSource(hikariConfig);
        Statement statement = ds.getConnection().createStatement();
        statement.execute("SELECT 1");
        ResultSet resultSet = statement.getResultSet();

        resultSet.next();
        int resultSetInt = resultSet.getInt(1);
        assertEquals("A basic SELECT query succeeds", 1, resultSetInt);
    }

    private DataSource getDataSource() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        URLClassLoader urlClassLoader = URLClassLoader.newInstance(JdbcDriverUtil.getDrivers("../../ext-lib/virtjdbc*"), this.getClass().getClassLoader());
        DataSource dataSource = (DataSource) Class.forName("virtuoso.jdbc4.VirtuosoDataSource",true, urlClassLoader).newInstance();
        Properties properties = new Properties();
        properties.put("serverName",virtuoso.getContainerIpAddress());
        properties.put("portNumber", virtuoso.getMappedPort(virtuoso.JDBC_PORT));
        properties.put("user", virtuoso.getUsername());
        properties.put("password", virtuoso.getPassword());
        PropertyElf.setTargetFromProperties(dataSource, properties);
        return dataSource;
    }
}
