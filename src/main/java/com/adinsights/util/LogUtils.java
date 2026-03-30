package com.adinsights.util;

import net.logstash.logback.argument.StructuredArguments;

public final class LogUtils {

    private LogUtils() {
    }

    public static Object kv(String key, Object value) {
        return StructuredArguments.keyValue(key, value);
    }
}