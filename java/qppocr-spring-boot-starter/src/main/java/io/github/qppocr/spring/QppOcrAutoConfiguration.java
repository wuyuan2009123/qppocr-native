package io.github.qppocr.spring;

import io.github.qppocr.core.OcrEngine;
import io.github.qppocr.core.OcrOptions;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

/** 装配 {@link OcrEngine}（单例，随容器关闭销毁）。 */
@AutoConfiguration
@EnableConfigurationProperties(QppOcrProperties.class)
@ConditionalOnProperty(prefix = "qppocr", name = "models-dir")
public class QppOcrAutoConfiguration {

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public OcrEngine ocrEngine(QppOcrProperties props) {
        if (StringUtils.hasText(props.getLibraryPath())) {
            System.setProperty("qppocr.library", props.getLibraryPath());
        }
        OcrOptions.Builder b = OcrOptions.builder(props.getModelsDir())
                .tier(props.getTier())
                .threads(props.getThreads())
                .detectOrientation(props.isDetectOrientation())
                .verifySha256(props.isVerifySha256())
                .device(props.getDevice());
        if (props.getPreset() != null) {
            b.preset(props.getPreset());
        }
        return new OcrEngine(b.build());
    }
}
