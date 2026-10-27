package io.github.qppocr.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** 一行文本（阅读序），坐标均为原图空间。 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TextLine {

    private String text;
    private List<CharSpan> chars;
    private float confidence;
    private int rotation;
    private boolean retried;
    /** 四角点 TL/TR/BR/BL */
    private float[][] pts;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<CharSpan> getChars() {
        return chars;
    }

    public void setChars(List<CharSpan> chars) {
        this.chars = chars;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public boolean isRetried() {
        return retried;
    }

    public void setRetried(boolean retried) {
        this.retried = retried;
    }

    public float[][] getPts() {
        return pts;
    }

    public void setPts(float[][] pts) {
        this.pts = pts;
    }
}
