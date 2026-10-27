# qppocr-native

为 [qppocr](https://github.com/qinwenhui/qppocr)（纯 Rust PP-OCRv6 推理引擎）提供的多语言接入层：

| 模块 | 说明 |
|---|---|
| `capi/` | Rust cdylib，C ABI 绑定 + `qppocr.h`（不内置 qppocr 源码，依赖 crates.io `qppocr = 0.3.1`） |
| `java/qppocr-core` | Java 接入核心：JNA 映射 + 按 OS/Arch 自动加载 dll/so/dylib |
| `java/qppocr-spring-boot-starter` | Spring Boot 4 Starter，配置即装配 `OcrEngine` Bean |
| `java/qppocr-solon-plugin` | Solon 4 插件，配置即装配 `OcrEngine` Bean |
| `java/qppocr-server-springboot` | OCR HTTP 服务（Spring Boot 版），`POST /ocr` |
| `java/qppocr-server-solon` | OCR HTTP 服务（Solon 版），`POST /ocr` |

## C API

```c
qppocr_engine_t *e = qppocr_create("models/", "tiny", "balanced", 0, -1, 1, "cpu");
char *json = qppocr_run_file(e, "receipt.png");   // 或 qppocr_run_image(e, bytes, len)
if (json == NULL) { fprintf(stderr, "%s\n", qppocr_last_error()); }
/* 使用 json ... */
qppocr_free_string(json);
qppocr_destroy(e);
```

- device 参数：默认 cpu；`auto` = 先试 GPU，不可用则打一条 WARN 并回退 CPU（Java 侧实现）；`gpu/vulkan` 为显式指定，不可用直接报错不降级。GPU 需 Vulkan 1.4+，CI 产物已启用 gpu feature。
- 并发安全：`Engine` 为 `Send+Sync`，同一句柄可多线程并发 `run`；错误信息为线程本地。
- `qppocr_destroy` 必须在无并发调用时执行（Java 侧已用读写锁兜底）。
- 返回 JSON：`lines[].text/confidence/pts/chars[]`（逐字坐标）+ `timings`（9 项分阶段毫秒）。

## CI / Release

`.github/workflows/build-native.yml`：
- 矩阵构建 6 个目标：Windows x86_64/ARM64（dll）、Linux x86_64/aarch64（so）、macOS x86_64/aarch64（dylib）
- Maven 打包 java 模块
- **打 tag（`v*`）时**，native zip（含头文件）+ core/starter/plugin/server jar 全部挂到 GitHub Release

## Java core

```java
OcrOptions opts = OcrOptions.builder("models/").tier(Tier.TINY).build();
try (OcrEngine engine = new OcrEngine(opts)) {
    OcrResult r = engine.run(Files.readAllBytes(Path.of("receipt.png")));
    System.out.println(r.plainText());
}
```

原生库加载顺序：`-Dqppocr.library=/path/to/lib`（或环境变量 `QPPOCR_LIBRARY`）
→ classpath `/natives/<os>-<arch>/` 内嵌库。把 CI 产物按此目录放进资源即可随 jar 分发。

## Spring Boot 4 Starter

```yaml
qppocr:
  models-dir: ./models
  tier: tiny          # tiny/small/medium
  preset: balanced    # speed/balanced/accuracy
  threads: 0          # <=0 自动
  device: auto        # cpu/auto/gpu/vulkan/vulkan:N/cuda；auto = GPU 优先、不可用回退 CPU
```

注入即可用：`private final OcrEngine engine;`

## Solon Plugin

```yaml
qppocr:
  modelsDir: ./models
  tier: tiny
```

`@Inject private OcrEngine engine;`

## OCR Server

两个 server 接口一致：`POST /ocr`，multipart 字段名 `file`，返回识别 JSON。

```bash
# Spring Boot 版（8080）
java -jar java/qppocr-server-springboot/target/qppocr-server-springboot-0.1.0.jar \
  --qppocr.models-dir=./models --qppocr.tier=tiny

# Solon 版（8081，开发期带 slf4j-simple provided 日志）
mvn -f java/qppocr-server-solon dependency:build-classpath -Dmdep.includeScope=provided
java -cp "target/classes;<deps>" io.github.qppocr.server.QppOcrSolonServer

curl -F file=@receipt.png http://localhost:8080/ocr
```

## 模型

仓库内置 tiny 档（det/rec/字典）可直接跑；其余档位按下方说明自行下载到 `models/`（不入库）。

布局（tiny 已验证）：

```
models/
├── tiny/  det.onnx + rec.onnx + dict.txt   # tiny 字典 6,904 行
├── small/ det.onnx + rec.onnx
├── cls.onnx                                 # 可选，方向分类
└── dict.txt                                 # small/medium 共用 18,708 行
```

- 模型：HuggingFace `PaddlePaddle/PP-OCRv6_{tier}_{det|rec}_onnx` 的 `inference.onnx`，装载时自动校验 SHA-256。
- 字典：PaddleOCR 仓库 `ppocr/utils/dict/ppocrv6_dict.txt`（small/medium）与 `ppocrv6_tiny_dict.txt`（tiny）。
- 字典查找顺序：`{tier}/dict.txt` → `dict.txt` → `ppocr_keys.txt` → rec 内嵌。
