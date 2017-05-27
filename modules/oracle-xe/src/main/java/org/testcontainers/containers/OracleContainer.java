package org.testcontainers.containers;

import org.junit.runner.Description;

import static org.junit.Assume.assumeNoException;

/**
 * @author gusohal
 */
public class OracleContainer<SELF extends OracleContainer<SELF>> extends JdbcDatabaseContainer<SELF> {

    public static final String NAME = "oracle";
    public static final String IMAGE = "wnameless/oracle-xe-11g";
    private static final int ORACLE_PORT = 1521;
    private static final int APEX_HTTP_PORT = 8080;

    public OracleContainer() {
        super(IMAGE + ":latest");
    }

    public OracleContainer(String dockerImageName) {
        super(dockerImageName);
    }

    @Override
    protected void starting(Description description) {
        try {
            getJdbcDriverInstance();
        } catch (Exception e) {
            assumeNoException(description.toString(), e);
        }
        super.starting(description);
    }

    @Override
    protected Integer getLivenessCheckPort() {
        return getMappedPort(ORACLE_PORT);
    }

    @Override
    protected void configure() {

        addExposedPorts(ORACLE_PORT, APEX_HTTP_PORT);
    }

    @Override
    public String getDriverClassName() {
        return "oracle.jdbc.OracleDriver";
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:oracle:thin:" + getUsername() + "/" + getPassword() + "@//" + getContainerIpAddress() + ":" + getOraclePort() + "/" + getSid();
    }

    @Override
    public String getUsername() {
        return "system";
    }

    @Override
    public String getPassword() {
        return "oracle";
    }

    @SuppressWarnings("SameReturnValue")
    public String getSid() {
        return "xe";
    }

    public Integer getOraclePort() {
        return getMappedPort(ORACLE_PORT);
    }

    @SuppressWarnings("unused")
    public Integer getWebPort() {
        return getMappedPort(APEX_HTTP_PORT);
    }

    @Override
    public String getTestQueryString() {
        return "SELECT 1 FROM DUAL";
    }
}