package io.github.qppocr.solon.integration;

import io.github.qppocr.core.OcrEngine;
import io.github.qppocr.core.OcrOptions;
import io.github.qppocr.core.Preset;
import io.github.qppocr.core.Tier;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;

/**
 * qppocr 的 Solon 插件：读取 {@code qppocr.*} 配置，注册 {@link OcrEngine} 单例 Bean。
 *
 * <pre>
 * qppocr:
 *   modelsDir: ./models        # 必填；缺省不装配
 *   tier: small                # tiny/small/medium
 *   preset: balanced           # speed/balanced/accuracy
 *   threads: 0                 # <=0 自动
 *   detectOrientation: true
 *   verifySha256: true
 *   libraryPath:               # 可选：直接指定 dll/so/dylib
 * </pre>
 */
public class QppOcrSolonPlugin implements Plugin {

    private OcrEngine engine;

    @Override
    public void start(AppContext context) {
        String modelsDir = context.cfg().get("qppocr.modelsDir");
        if (modelsDir == null || modelsDir.isBlank()) {
            return;
        }

        String libraryPath = context.cfg().get("qppocr.libraryPath");
        if (libraryPath != null && !libraryPath.isBlank()) {
            System.setProperty("qppocr.library", libraryPath);
        }

        OcrOptions.Builder b = OcrOptions.builder(modelsDir)
                .tier(parseEnum(Tier.class, context.cfg().get("qppocr.tier"), Tier.SMALL))
                .threads(context.cfg().getInt("qppocr.threads", 0))
                .detectOrientation(context.cfg().getBool("qppocr.detectOrientation", true))
                .verifySha256(context.cfg().getBool("qppocr.verifySha256", true))
                .device(context.cfg().get("qppocr.device", "cpu"));
        Preset preset = parseEnum(Preset.class, context.cfg().get("qppocr.preset"), null);
        if (preset != null) {
            b.preset(preset);
        }

        engine = new OcrEngine(b.build());
        context.wrapAndPut(OcrEngine.class, engine);
    }

    @Override
    public void stop() {
        if (engine != null) {
            engine.close();
            engine = null;
        }
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String value, E fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return Enum.valueOf(type, value.trim().toUpperCase());
    }
}
