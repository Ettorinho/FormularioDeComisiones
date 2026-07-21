package com.comisiones.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppLogger {
    private static final StackWalker STACK_WALKER =
            StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    public static void debug(String message) {
        resolveLogger().debug(message);
    }

    public static void info(String message) {
        resolveLogger().info(message);
    }

    public static void warn(String message) {
        resolveLogger().warn(message);
    }

    public static void error(String message, Throwable throwable) {
        if (throwable == null) {
            resolveLogger().error(message);
            return;
        }
        resolveLogger().error(message, throwable);
    }

    public static void separator() {
        resolveLogger().debug("========================================");
    }

    private static Logger resolveLogger() {
        Class<?> callerClass = STACK_WALKER.walk(stream -> stream
                .skip(2)
                .findFirst()
                .map(StackWalker.StackFrame::getDeclaringClass)
                .orElse(AppLogger.class));
        return LoggerFactory.getLogger(callerClass);
    }
}
