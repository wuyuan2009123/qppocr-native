package io.github.qppocr.spring;

import io.github.qppocr.core.Preset;
import io.github.qppocr.core.Tier;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * qppocr 配置：
 * <pre>
 * qppocr:
 *   models-dir: ./models        # 必填
 *   tier: small                 # tiny/small/medium
 *   preset: balanced            # speed/balanced/accuracy
 *   threads: 0                  # <=0 自动
 *   detect-orientation: true
 *   verify-sha256: true
 *   library-path:               # 可选：直接指定 dll/so/dylib 路径
 * </pre>
 */
@ConfigurationProperties(prefix = "qppocr")
public class QppOcrProperties {

    /** 模型目录（必填） */
    private String modelsDir;

    /** 模型档位 */
    private Tier tier = Tier.SMALL;

    /** 预设，null 用引擎默认 */
    private Preset preset;

    /** 推理线程数，<=0 自动 */
    private int threads = 0;

    /** 0/180 方向分类 */
    private boolean detectOrientation = true;

    /** 装载时校验模型 SHA-256 */
    private boolean verifySha256 = true;

    /** 计算设备：cpu/gpu/vulkan/vulkan:N/cuda，GPU 需 gpu 版原生库 */
    private String device = "cpu";

    /** 原生库显式路径（等价于 -Dqppocr.library） */
    private String libraryPath;

    public String getModelsDir() {
        return modelsDir;
    }

    public void setModelsDir(String modelsDir) {
        this.modelsDir = modelsDir;
    }

    public Tier getTier() {
        return tier;
    }

    public void setTier(Tier tier) {
        this.tier = tier;
    }

    public Preset getPreset() {
        return preset;
    }

    public void setPreset(Preset preset) {
        this.preset = preset;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public boolean isDetectOrientation() {
        return detectOrientation;
    }

    public void setDetectOrientation(boolean detectOrientation) {
        this.detectOrientation = detectOrientation;
    }

    public boolean isVerifySha256() {
        return verifySha256;
    }

    public void setVerifySha256(boolean verifySha256) {
        this.verifySha256 = verifySha256;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getLibraryPath() {
        return libraryPath;
    }

    public void setLibraryPath(String libraryPath) {
        this.libraryPath = libraryPath;
    }
}
