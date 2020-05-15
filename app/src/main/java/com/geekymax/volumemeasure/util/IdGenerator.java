package com.geekymax.volumemeasure.util;

import java.util.UUID;

public class IdGenerator {
    public static String genUuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
