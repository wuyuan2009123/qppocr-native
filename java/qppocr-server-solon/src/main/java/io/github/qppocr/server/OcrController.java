package io.github.qppocr.server;

import io.github.qppocr.core.OcrEngine;
import io.github.qppocr.core.OcrResult;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Param;
import org.noear.solon.annotation.Post;
import org.noear.solon.core.handle.UploadedFile;

import java.io.IOException;

@Controller
public class OcrController {

    @Inject
    private OcrEngine engine;

    /** POST /ocr，multipart 字段名 file，内容为 PNG/JPEG/BMP 图片。 */
    @Post
    @Mapping("/ocr")
    public OcrResult ocr(@Param("file") UploadedFile file) throws IOException {
        return engine.run(file.getContent().readAllBytes());
    }
}
