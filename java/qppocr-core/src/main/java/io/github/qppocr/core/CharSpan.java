package io.github.qppocr.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** 单个字符及其四边形坐标（原图空间，TL/TR/BR/BL）。 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CharSpan {

    private String text;
    private float[][] pts;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float[][] getPts() {
        return pts;
    }

    public void setPts(float[][] pts) {
        this.pts = pts;
    }
}
