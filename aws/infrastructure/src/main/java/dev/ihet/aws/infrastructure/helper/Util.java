package dev.ihet.aws.infrastructure.helper;

public class Util {

    private static final Configuration config = Configuration.load();

    public static String resourceId(String suffix) {
        return config.name + suffix;
    }
}
