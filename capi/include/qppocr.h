/*
 * qppocr C API —— PP-OCRv6 纯 Rust 推理引擎的 C ABI 绑定。
 *
 * 线程安全：同一 engine 句柄可多线程并发调用 run；错误信息为线程本地。
 * 内存约定：qppocr_run_* 返回的字符串必须调用 qppocr_free_string 释放；
 *           qppocr_last_error / qppocr_version 返回的字符串为只读，不可释放。
 */
#ifndef QPPOCR_H
#define QPPOCR_H

#include <stddef.h>

#ifdef _WIN32
#  ifdef QPPOCR_BUILDING
#    define QPPOCR_API __declspec(dllexport)
#  else
#    define QPPOCR_API __declspec(dllimport)
#  endif
#else
#  define QPPOCR_API __attribute__((visibility("default")))
#endif

#ifdef __cplusplus
extern "C" {
#endif

/* 不透明引擎句柄 */
typedef struct qppocr_engine qppocr_engine_t;

/* 创建引擎。
 * models_dir        模型目录（必需）
 * tier              "tiny" / "small" / "medium"，NULL = "small"
 * preset            "speed" / "balanced" / "accuracy"，NULL = 默认
 * threads           <=0 表示自动
 * detect_orientation -1 默认，0 关，1 开
 * verify_sha256     0 跳过模型哈希校验，非 0 校验（默认）
 * device            "cpu" / "gpu" / "vulkan" / "vulkan:N" / "cuda"，NULL = "cpu"
 *                   GPU 需编译期启用 gpu feature；显式要 GPU 而不可用时创建失败，
 *                   不静默回退 CPU。
 * 返回：成功为引擎句柄，失败为 NULL（见 qppocr_last_error）
 */
QPPOCR_API qppocr_engine_t *qppocr_create(
    const char *models_dir,
    const char *tier,
    const char *preset,
    int threads,
    int detect_orientation,
    int verify_sha256,
    const char *device);

/* 销毁引擎（NULL 安全） */
QPPOCR_API void qppocr_destroy(qppocr_engine_t *engine);

/* 识别图片文件（PNG/JPEG/BMP）。
 * 返回 JSON 字符串（调用方用 qppocr_free_string 释放）；失败返回 NULL。
 */
QPPOCR_API char *qppocr_run_file(qppocr_engine_t *engine, const char *path);

/* 识别内存中的图片字节（PNG/JPEG/BMP）。
 * 返回 JSON 字符串（调用方用 qppocr_free_string 释放）；失败返回 NULL。
 */
QPPOCR_API char *qppocr_run_image(qppocr_engine_t *engine,
                                  const unsigned char *data,
                                  size_t len);

/* 释放 qppocr_run_* 返回的字符串（NULL 安全） */
QPPOCR_API void qppocr_free_string(char *s);

/* 当前线程最近一次错误信息（UTF-8 只读，无错误时返回 NULL） */
QPPOCR_API const char *qppocr_last_error(void);

/* 版本号字符串（静态只读） */
QPPOCR_API const char *qppocr_version(void);

#ifdef __cplusplus
}
#endif

#endif /* QPPOCR_H */
