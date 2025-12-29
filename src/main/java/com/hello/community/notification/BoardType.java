// BoardType.java
package com.hello.community.notification;

public enum BoardType {
    NEWS,
    MUSIC,
    NOTICE;

    public static BoardType fromPath(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("boardType is null");
        }

        String v = raw.trim().toLowerCase();

        if (v.equals("news")) {
            return NEWS;
        }
        if (v.equals("music")) {
            return MUSIC;
        }
        if (v.equals("notice")) {
            return NOTICE;
        }

        throw new IllegalArgumentException("unknown boardType: " + raw);
    }

    public String toPath() {
        return this.name().toLowerCase();
    }
}
