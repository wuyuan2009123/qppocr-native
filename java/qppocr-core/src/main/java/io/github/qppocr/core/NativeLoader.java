package io.github.qppocr.core;

import com.sun.jna.Library;
import com.sun.jna.Native;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * 原生库加载器：
 * <ol>
 *   <li>系统属性 {@code qppocr.library} / 环境变量 {@code QPPOCR_LIBRARY} 指向库文件</li>
 *   <li>否则从 classpath {@code /natives/&lt;os&gt;-&lt;arch&gt;/&lt;libfile&gt;} 解压到临时目录加载</li>
 * </ol>
 * 打包时 natives 目录由 CI（build-native workflow 产物）填充。
 */
final class NativeLoader {

    private static volatile QppOcrLibrary instance;

    private NativeLoader() {
    }

    static QppOcrLibrary get() {
        QppOcrLibrary lib = instance;
        if (lib == null) {
            synchronized (NativeLoader.class) {
                lib = instance;
                if (lib == null) {
                    lib = load();
                    instance = lib;
                }
            }
        }
        return lib;
    }

    private static QppOcrLibrary load() {
        Map<String, Object> options = new HashMap<>();
        options.put(Library.OPTION_STRING_ENCODING, "UTF-8");
        return Native.load(resolveLibraryPath(), QppOcrLibrary.class, options);
    }

    private static String resolveLibraryPath() {
        String explicit = System.getProperty("qppocr.library");
        if (explicit == null || explicit.isBlank()) {
            explicit = System.getenv("QPPOCR_LIBRARY");
        }
        if (explicit != null && !explicit.isBlank()) {
            return explicit;
        }

        String os = os();
        String arch = arch();
        String libFile = switch (os) {
            case "windows" -> "qppocr_capi.dll";
            case "macos" -> "libqppocr_capi.dylib";
            default -> "libqppocr_capi.so";
        };
        String resource = "/natives/" + os + "-" + arch + "/" + libFile;
        try (InputStream in = NativeLoader.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new OcrException("未找到原生库资源 " + resource
                        + "。请放入对应平台的 dll/so/dylib，或用 -Dqppocr.library=/path/to/lib 指定");
            }
            Path tmp = Files.createTempDirectory("qppocr-native-");
            tmp.toFile().deleteOnExit();
            Path target = tmp.resolve(libFile);
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            target.toFile().deleteOnExit();
            return target.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new OcrException("解压原生库失败: " + e.getMessage(), e);
        }
    }

    private static String os() {
        String name = System.getProperty("os.name", "").toLowerCase();
        if (name.contains("win")) {
            return "windows";
        }
        if (name.contains("mac") || name.contains("darwin")) {
            return "macos";
        }
        return "linux";
    }

    private static String arch() {
        String a = System.getProperty("os.arch", "").toLowerCase();
        if (a.equals("aarch64") || a.equals("arm64")) {
            return "aarch64";
        }
        return "x86_64";
    }
}
