package io.github.qppocr.core;

import java.util.Objects;

/** 引擎创建参数。 */
public final class OcrOptions {

    private final String modelsDir;
    private final Tier tier;
    private final Preset preset;
    private final int threads;
    /** null = 引擎默认（开） */
    private final Boolean detectOrientation;
    private final boolean verifySha256;
    /**
     * 计算设备：
     * <ul>
     *   <li>{@code cpu}（默认）</li>
     *   <li>{@code auto} —— GPU 优先，不可用时自动回退 CPU</li>
     *   <li>{@code gpu} / {@code vulkan} / {@code vulkan:N} / {@code cuda} —— 显式指定，不可用即报错</li>
     * </ul>
     */
    private final String device;

    private OcrOptions(Builder b) {
        this.modelsDir = Objects.requireNonNull(b.modelsDir, "modelsDir 不能为空");
        this.tier = b.tier;
        this.preset = b.preset;
        this.threads = b.threads;
        this.detectOrientation = b.detectOrientation;
        this.verifySha256 = b.verifySha256;
        this.device = b.device;
    }

    public String modelsDir() {
        return modelsDir;
    }

    public Tier tier() {
        return tier;
    }

    public Preset preset() {
        return preset;
    }

    public int threads() {
        return threads;
    }

    public Boolean detectOrientation() {
        return detectOrientation;
    }

    public boolean verifySha256() {
        return verifySha256;
    }

    public String device() {
        return device;
    }

    public static Builder builder(String modelsDir) {
        return new Builder(modelsDir);
    }

    public static final class Builder {
        private final String modelsDir;
        private Tier tier = Tier.SMALL;
        private Preset preset;
        private int threads = 0;
        private Boolean detectOrientation;
        private boolean verifySha256 = true;
        private String device = "cpu";

        private Builder(String modelsDir) {
            this.modelsDir = modelsDir;
        }

        public Builder tier(Tier tier) {
            this.tier = tier;
            return this;
        }

        public Builder preset(Preset preset) {
            this.preset = preset;
            return this;
        }

        public Builder threads(int threads) {
            this.threads = threads;
            return this;
        }

        public Builder detectOrientation(boolean on) {
            this.detectOrientation = on;
            return this;
        }

        public Builder verifySha256(boolean verify) {
            this.verifySha256 = verify;
            return this;
        }

        public Builder device(String device) {
            this.device = device;
            return this;
        }

        public OcrOptions build() {
            return new OcrOptions(this);
        }
    }
}
