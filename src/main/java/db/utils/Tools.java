package db.utils;

import java.util.Date;

public final class Tools {
    public static final String DEFAULT_HOST = "//localhost/";
    public static final int DEFAULT_PORT = 8088;
    public static final int DEFAULT_THREADS_IN = 10;
    public static final int DEFAULT_THREADS_OUT = 10;
    public static final int DEFAULT_REQUESTS_CAPACITY = 10_000;
    public static final int DEFAULT_RESPONSES_CAPACITY = 10_000;
    public static final String SYSTEM_USER_ID = "0";

    public static void log(final String message, final Object... args) {
        System.out.printf("%s%n\t>>> %s%n", new Date(System.currentTimeMillis()), message.formatted(args));
    }
}
