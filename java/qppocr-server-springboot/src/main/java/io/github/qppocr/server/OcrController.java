package io.github.qppocr.server;

import io.github.qppocr.core.OcrEngine;
import io.github.qppocr.core.OcrException;
import io.github.qppocr.core.OcrResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
public class OcrController {

    private final OcrEngine engine;

    public OcrController(OcrEngine engine) {
        this.engine = engine;
    }

    /** POST /ocr，multipart 字段名 file，内容为 PNG/JPEG/BMP 图片。 */
    @PostMapping("/ocr")
    public OcrResult ocr(@RequestParam("file") MultipartFile file) throws IOException {
        return engine.run(file.getBytes());
    }

    @ExceptionHandler(OcrException.class)
    public ResponseEntity<Map<String, String>> onOcrError(OcrException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", e.getMessage()));
    }
}
