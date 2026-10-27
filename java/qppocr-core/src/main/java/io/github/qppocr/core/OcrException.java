package io.github.qppocr.core;

/** qppocr 调用失败（引擎创建、推理、原生库加载）。 */
public class OcrException extends RuntimeException {

    public OcrException(String message) {
        super(message);
    }

    public OcrException(String message, Throwable cause) {
        super(message, cause);
    }
}
