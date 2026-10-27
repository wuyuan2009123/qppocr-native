package io.github.qppocr.core;

/** 模型档位（与 qppocr Tier 对应，目录名 = 小写名）。 */
public enum Tier {
    TINY("tiny"),
    SMALL("small"),
    MEDIUM("medium");

    private final String dirName;

    Tier(String dirName) {
        this.dirName = dirName;
    }

    public String dirName() {
        return dirName;
    }
}
