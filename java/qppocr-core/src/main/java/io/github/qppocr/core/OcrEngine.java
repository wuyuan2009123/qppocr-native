package io.github.qppocr.core;

import tools.jackson.databind.ObjectMapper;
import com.sun.jna.Pointer;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * OCR 引擎句柄（线程安全，AutoCloseable）。
 *
 * <p>qppocr 的 Engine 是 Send+Sync，{@link #run} 系列可多线程并发调用。
 * {@link #close()} 用写锁与 run 的读锁互斥，保证销毁时没有进行中的推理。
 */
public final class OcrEngine implements AutoCloseable {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final Logger LOG = System.getLogger("io.github.qppocr");

    private final QppOcrLibrary lib;
    private Pointer engine;
    private String activeDevice;

    public OcrEngine(OcrOptions options) {
        this.lib = NativeLoader.get();
        String device = options.device() == null ? "cpu" : options.device();
        if ("auto".equalsIgnoreCase(device)) {
            // GPU 优先，不可用回退 CPU（显式指定设备的场景不做静默降级）
            Pointer gpu = create(options, "gpu");
            if (gpu != null) {
                this.engine = gpu;
                this.activeDevice = "gpu";
                return;
            }
            String gpuError = lastError();
            LOG.log(Level.WARNING, "qppocr: GPU 不可用，回退 CPU。原因: " + gpuError);
            device = "cpu";
        }
        Pointer p = create(options, device);
        if (p == null) {
            throw new OcrException("引擎创建失败: " + lastError());
        }
        this.engine = p;
        this.activeDevice = device;
    }

    private Pointer create(OcrOptions options, String device) {
        return lib.qppocr_create(
                options.modelsDir(),
                options.tier().dirName(),
                options.preset() == null ? null : options.preset().wireName(),
                options.threads(),
                options.detectOrientation() == null ? -1 : (options.detectOrientation() ? 1 : 0),
                options.verifySha256() ? 1 : 0,
                device);
    }

    /** 实际生效的设备名（{@code auto} 时会解析为 gpu 或 cpu）。 */
    public String device() {
        return activeDevice;
    }

    /** 识别图片文件（PNG/JPEG/BMP）。 */
    public OcrResult run(Path file) {
        return parse(runJson(file.toAbsolutePath().toString(), null));
    }

    /** 识别内存中的图片字节（PNG/JPEG/BMP）。 */
    public OcrResult run(byte[] imageBytes) {
        return parse(runJson(null, imageBytes));
    }

    /** 识别并返回原始 JSON 字符串（文件路径入参）。 */
    public String runJson(Path file) {
        return runJson(file.toAbsolutePath().toString(), null);
    }

    /** 识别并返回原始 JSON 字符串（字节入参）。 */
    public String runJson(byte[] imageBytes) {
        return runJson(null, imageBytes);
    }

    private String runJson(String path, byte[] bytes) {
        lock.readLock().lock();
        try {
            if (engine == null) {
                throw new OcrException("引擎已关闭");
            }
            Pointer out = path != null
                    ? lib.qppocr_run_file(engine, path)
                    : lib.qppocr_run_image(engine, bytes, bytes.length);
            if (out == null) {
                throw new OcrException("识别失败: " + lastError());
            }
            try {
                return out.getString(0, "UTF-8");
            } finally {
                lib.qppocr_free_string(out);
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    private String lastError() {
        String err = lib.qppocr_last_error(); // JNA 已按 UTF-8 解码
        return err == null ? "未知错误" : err;
    }

    private static OcrResult parse(String json) {
        try {
            return MAPPER.readValue(json, OcrResult.class);
        } catch (Exception e) {
            throw new OcrException("结果 JSON 解析失败: " + e.getMessage(), e);
        }
    }

    /** qppocr-capi 版本号。 */
    public static String nativeVersion() {
        return NativeLoader.get().qppocr_version();
    }

    @Override
    public void close() {
        lock.writeLock().lock();
        try {
            if (engine != null) {
                lib.qppocr_destroy(engine);
                engine = null;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}
