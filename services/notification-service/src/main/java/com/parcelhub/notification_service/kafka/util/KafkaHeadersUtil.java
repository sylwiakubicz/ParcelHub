package com.parcelhub.notification_service.kafka.util;

import org.apache.kafka.common.header.Headers;

import java.nio.ByteBuffer;

public class KafkaHeadersUtil {

    private KafkaHeadersUtil() {}

    public static int getInt(Headers headers, String name, int def) {
        var h = headers.lastHeader(name);
        if (h == null) { return def; }
        return ByteBuffer.wrap(h.value()).getInt();
    }

    public static long getLong(Headers headers, String name, long def) {
        var h = headers.lastHeader(name);
        if (h == null) { return def; }
        return ByteBuffer.wrap(h.value()).getLong();
    }

    public static void putInt(Headers headers, String name, int val) {
        headers.remove(name);
        headers.add(name, ByteBuffer.allocate(4).putInt(val).array());
    }

    public static void putLong(Headers headers, String name, long val) {
        headers.remove(name);
        headers.add(name, ByteBuffer.allocate(8).putLong(val).array());
    }
}
