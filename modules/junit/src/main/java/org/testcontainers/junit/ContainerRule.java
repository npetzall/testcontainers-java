package org.testcontainers.junit;

import org.junit.rules.TestRule;

public interface ContainerRule<SELF> extends TestRule {

    default SELF self() {
        return (SELF) this;
    }

}
