package org.testcontainers.junit;

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GenericContainerRule<T extends GenericContainer> implements TestRule {

    private Supplier<T> containerSupplier;
    private T container;
    private List<Consumer<T>> assumptions = new ArrayList<>();

    public GenericContainerRule(Supplier<T> containerSupplier) {
        this.containerSupplier = containerSupplier;
    }

    public GenericContainerRule<T> assumeDockerIsPresent() {
        final Supplier<T> originalContainerSupplier = containerSupplier;
        containerSupplier = () -> {
          try {
              DockerClientFactory.instance().client();
          } catch (Throwable t) {
              throw new AssumptionViolatedException("Unable to create container[might be docker related]");
          }
            return originalContainerSupplier.get();
        };
        return this;
    }

    public GenericContainerRule withAssumptions(Consumer<T>...assumptions) {
        for (Consumer<T> consumer : assumptions) {
            this.assumptions.add(consumer);
        }
        return this;
    }

    public T getContainer() {
        return container;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        container = containerSupplier.get();
        assumptions.forEach(a -> a.accept(container));
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                List<Throwable> errors = new ArrayList<Throwable>();

                try {
                    container.start();
                    base.evaluate();
                } catch (Throwable e) {
                    errors.add(e);
                } finally {
                    container.stop();
                }

                MultipleFailureException.assertEmpty(errors);
            }
        };
    }
}
