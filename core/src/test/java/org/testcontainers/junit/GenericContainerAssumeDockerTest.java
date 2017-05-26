package org.testcontainers.junit;

import com.github.dockerjava.api.DockerClient;
import org.hamcrest.CoreMatchers;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.junit.runner.Description;
import org.testcontainers.containers.GenericContainer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import static org.rnorth.visibleassertions.VisibleAssertions.assertThat;

public class GenericContainerAssumeDockerTest {

    @Test
    public void whenDockerClientCantBeCreatedAnAssumptionExceptionIsThrown() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        GenericContainer genericContainer = new GenericContainer().assumeDocker();
        setFailingDockerClientSupplier(genericContainer);
        boolean assumptionExceptionOccurred = false;
        try {
            callAssumptions(genericContainer);
        } catch (InvocationTargetException ite) {
            if (ite.getCause() instanceof AssumptionViolatedException) {
                assumptionExceptionOccurred = true;
            }
        }
        assertThat("AssumptionViolatedException was thrown", assumptionExceptionOccurred, CoreMatchers.is(true));
    }

    private void setFailingDockerClientSupplier(GenericContainer genericContainer) throws IllegalAccessException, NoSuchFieldException {
        Field dockerClientSupplier = genericContainer.getClass().getDeclaredField("dockerClientSupplier");
        dockerClientSupplier.setAccessible(true);
        dockerClientSupplier.set(genericContainer, new Supplier<DockerClient>() {

            @Override
            public DockerClient get() {
                throw new RuntimeException("Can't create docker client");
            }
        });
    }

    private void callAssumptions(GenericContainer genericContainer) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method assumptions = genericContainer.getClass().getDeclaredMethod("assumptions", Description.class);
        assumptions.setAccessible(true);
        assumptions.invoke(genericContainer, Description.createSuiteDescription("FailedAssumptionTest"));
    }
}
