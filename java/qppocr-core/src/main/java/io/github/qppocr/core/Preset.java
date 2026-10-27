package io.github.qppocr.core;

/** 预设（与 qppocr Preset 对应）。 */
public enum Preset {
    SPEED("speed"),
    BALANCED("balanced"),
    ACCURACY("accuracy");

    private final String wireName;

    Preset(String wireName) {
        this.wireName = wireName;
    }

    public String wireName() {
        return wireName;
    }
}
