package org.testcontainers.utility;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class JdbcDriverUtil {

    public static class PathGlob {
        public final Path start;
        public final String glob;
        public final int maxDepth;

        public PathGlob(Path start, String glob, int maxDepth) {
            this.start = start;
            this.glob = glob;
            this.maxDepth = maxDepth;
        }
    }

    public static URL[] getDrivers(String...driverPaths) {
        List<URL> urls = new ArrayList<>();
        for(String driverPath : driverPaths) {
            urls.addAll(getDriversFromPath(driverPath));
        }
        return urls.toArray(new URL[urls.size()]);
    }

    public static List<URL> getDriversFromPath(String driverPath) {
        List<URL> urls = new ArrayList<>();
        PathGlob pathGlob = parse(Paths.get(driverPath));
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + pathGlob.glob);
        try (Stream<Path> pathStream = Files.walk(pathGlob.start, pathGlob.maxDepth, FileVisitOption.FOLLOW_LINKS)) {
            pathStream.filter(p -> pathMatcher.matches(p)).map(p -> {
                try {
                    return p.toUri().toURL();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return null;
                }
            }).filter(u -> u != null)
                    .forEach(u -> urls.add(u));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return urls;
    }


    public static PathGlob parse(Path input) {
        Path p;
        if (input.startsWith("~")) {
            p = Paths.get(System.getProperty("user.home")).resolve(input.subpath(1, input.getNameCount()));
        } else {
            p = input;
        }

        Path root;
        boolean done = false;
        boolean checkingDepth = false;
        Iterator<Path> pathIterator = p.iterator();
        if (p.isAbsolute()) {
            root = p.getRoot();
        } else {
            root = pathIterator.next();
        }
        int maxDepth = 0;
        while(pathIterator.hasNext() && !done) {
            Path path = pathIterator.next();
            if (path.toString().matches(".*[\\?\\*].*") && !checkingDepth) {;
                checkingDepth = true;
            } else if (!checkingDepth){
                root = root.resolve(path);
            }
            if (checkingDepth) {
                if (path.toString().equals("**")) {
                    maxDepth = Integer.MAX_VALUE;
                    done = true;
                } else {
                    maxDepth++;
                }
            }
        }
        return new PathGlob(root, p.toString(), maxDepth);
    }
}
