package org.testcontainers.junit;

import org.hamcrest.CoreMatchers;
import org.junit.AssumptionViolatedException;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.dockerclient.FailingDockerClientProviderStrategy;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import static org.rnorth.visibleassertions.VisibleAssertions.assertThat;
import static org.rnorth.visibleassertions.VisibleAssertions.fail;

public class GenericContainerRuleTest {

    @ClassRule
    public static GenericContainerRule genericContainerRule = new GenericContainerRule(() -> new GenericContainer<>().withCommand("ping -c 5 www.google.com")).assumeDockerIsPresent();

    @Test
    public void whenDockerClientCantBeCreatedAnAssumptionExceptionIsThrown() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        destroyDockerClientFactoryInstance();
        GenericContainerRule genericContainerRule = new GenericContainerRule(() -> new GenericContainer()).assumeDockerIsPresent();
        boolean assumptionExceptionOccurred = false;
        try {
            genericContainerRule.apply(null,null).evaluate();
        } catch (AssumptionViolatedException ave) {
            assumptionExceptionOccurred = true;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            unDestroyDockerClientFactoryInstance();
        }
        assertThat("AssumptionViolatedException was thrown", assumptionExceptionOccurred, CoreMatchers.is(true));
    }

    private void destroyDockerClientFactoryInstance() throws NoSuchFieldException, IllegalAccessException {
        DockerClientFactory instance = DockerClientFactory.instance();
        Field strategy = DockerClientFactory.class.getDeclaredField("strategy");
        strategy.setAccessible(true);
        strategy.set(instance, new FailingDockerClientProviderStrategy());
    }

    private void unDestroyDockerClientFactoryInstance() throws NoSuchFieldException, IllegalAccessException {
        DockerClientFactory instance = DockerClientFactory.instance();
        Field strategy = DockerClientFactory.class.getDeclaredField("strategy");
        strategy.setAccessible(true);
        strategy.set(instance, null);
    }

    @Test
    public void whenDockerIsPresentNoExceptionIsThrownAndContainerCanBeRetrieved() {
        assertThat("Container has been created and is running", genericContainerRule.getContainer().isRunning(), CoreMatchers.is(true));
    }

    @Test
    public void GenericContainerRuleWithJDBCContainer() throws Throwable {
        GenericContainerRule containerRule = new GenericContainerRule<>(() -> new MySQLContainer())
                .assumeDockerIsPresent()
                .withAssumptions(container -> {
                    try {
                        container.getJdbcDriverInstance();
                    } catch (Exception e) {
                        throw new AssumptionViolatedException("No driver");
                    }
                });
        try {
            containerRule.apply(null, null).evaluate();
            fail("Assumption exception not thrown");
        } catch (Exception e) {
            assertThat("Assumption exception was thrown", e, CoreMatchers.instanceOf(AssumptionViolatedException.class));
        }
    }
}
