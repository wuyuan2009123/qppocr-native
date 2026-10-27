package io.github.qppocr.core;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

/**
 * qppocr-capi 的 JNA 直映射。字符串一律 UTF-8（见 NativeLoader 的 load 选项）。
 *
 * <p>size_t 形参用 Java long：本库只支持 64 位目标（win64/linux64/mac64），
 * size_t 均为 64 位，与 JNA 的 long（int64）匹配。
 */
interface QppOcrLibrary extends Library {

    Pointer qppocr_create(String modelsDir, String tier, String preset,
                          int threads, int detectOrientation, int verifySha256,
                          String device);

    void qppocr_destroy(Pointer engine);

    /** 返回 Rust 侧分配的字符串指针，读取后必须 qppocr_free_string。 */
    Pointer qppocr_run_file(Pointer engine, String path);

    /** 返回 Rust 侧分配的字符串指针，读取后必须 qppocr_free_string。 */
    Pointer qppocr_run_image(Pointer engine, byte[] data, long len);

    void qppocr_free_string(Pointer s);

    /** 线程本地错误串，只读。 */
    String qppocr_last_error();

    String qppocr_version();
}
