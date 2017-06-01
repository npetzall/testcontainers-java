package org.testcontainers.utility;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.rnorth.visibleassertions.VisibleAssertions.assertThat;

public class JdbcDriverUtilTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void findAbsolutFilesUsingGlob() throws IOException {
        File a1 = temporaryFolder.newFolder("a1");
        File b1 = new File(a1, "b1");
        b1.mkdir();
        File b2 = new File(a1, "b2");
        b2.mkdir();
        File b1f = new File(b1, "b1f");
        b1f.createNewFile();
        File b2f = new File(b2, "b2f");
        b2f.createNewFile();

        String driverPath = temporaryFolder.getRoot().getPath() + "/a1/b?/b?f";
        URL[] expected = new URL[] {
                b1f.toURI().toURL(),
                b2f.toURI().toURL()
        };
        List<URL> urls = JdbcDriverUtil.getDriversFromPath(driverPath);
        assertThat("Should only find 2 paths", urls.size(), CoreMatchers.is(2) );
        assertThat("Expected paths are found", urls, CoreMatchers.hasItems(expected));
    }

    @Test
    public void findRelativeFileUsingGlob() {
        String driverPath = "src/main/java/org/*/*/*Wrapper.java";
        List<URL> urls = JdbcDriverUtil.getDriversFromPath(driverPath);
        assertThat("Should have singel url", urls.size(), CoreMatchers.is(1));
    }

    @Test
    public void findUsingGlobWhereRootIsMissingShouldNotThrowException() {
        String driverPath = "src/main/jaa/org/*/*/*Wrapper.java";
        List<URL> urls = JdbcDriverUtil.getDriversFromPath(driverPath);
    }

}