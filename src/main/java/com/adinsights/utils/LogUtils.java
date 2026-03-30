package com.adinsights.utils;

import net.logstash.logback.argument.StructuredArguments;

public class LogUtils {

    public static Object kv(String key, Object value) {
        return StructuredArguments.keyValue(key, value);
    }
}