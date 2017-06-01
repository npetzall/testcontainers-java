package org.testcontainers.utility;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    private static Pattern GLOB_CHARACTER_PATTERN = Pattern.compile("(\\?|\\*)");

    public static URL[] getDrivers(String...driverPaths) {
        List<URL> urls = new ArrayList<>();
        for(String driverPath : driverPaths) {
            for(String path : driverPath.split(":"))
                urls.addAll(getDriversFromPath(driverPath));
        }
        return urls.toArray(new URL[urls.size()]);
    }

    public static List<URL> getDriversFromPath(String driverPath) {
        if (isPathWithGlob(driverPath)) {
            return findUsingGlob(driverPath);
        } else {
            try {
                return Collections.singletonList(URI.create(driverPath).toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyList();
    }

    public static boolean isPathWithGlob(String driverPath) {
        return GLOB_CHARACTER_PATTERN.matcher(driverPath).find();
    }

    private static List<URL> findUsingGlob(String driverPath) {
        ArrayList<URL> urls = new ArrayList<>();
        PathGlob pathGlob = parse(driverPath);
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + pathGlob.glob);
        try (Stream<Path> pathStream = Files.walk(pathGlob.start, pathGlob.maxDepth, FileVisitOption.FOLLOW_LINKS)) {
            pathStream.filter(p -> {
                System.out.println(p.toString());
                return pathMatcher.matches(p);
            }).map(p -> {
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


    public static PathGlob parse(String input) {
        String path = input.replaceAll("\\\\", "/");
        if (path.startsWith("~/")) {
            path = path.replace("~/", System.getProperty("user.home"));
        } else if (path.matches("(?!)^$HOME.*")){
            path = path.replace("$HOME", System.getProperty("user.home"));
        }
        Matcher matcher = GLOB_CHARACTER_PATTERN.matcher(path);
        matcher.find();
        int firstGlobCharacter = matcher.start();
        Path  root;
        int globStart = path.lastIndexOf("/", firstGlobCharacter);
        root = Paths.get(path.substring(0, globStart));
        String glob = path.substring(globStart);
        if (glob.contains("**")) {
            return new PathGlob(root, path, Integer.MAX_VALUE);
        }
        if (glob.contains("/")) {
            return new PathGlob(root, path, glob.split("/").length);
        }
        return new PathGlob(root, path, 1);
    }
}
