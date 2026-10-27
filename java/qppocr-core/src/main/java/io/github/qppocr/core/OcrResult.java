package io.github.qppocr.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** 一次 OCR 的结果（与 qppocr-core 的 JSON 导出对应，字段名 snake_case）。 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OcrResult {

    private List<TextLine> lines;
    @JsonProperty("work_w")
    private int workW;
    @JsonProperty("work_h")
    private int workH;
    @JsonProperty("det_input_w")
    private int detInputW;
    @JsonProperty("det_input_h")
    private int detInputH;
    @JsonProperty("num_boxes")
    private int numBoxes;
    @JsonProperty("num_merged")
    private int numMerged;
    @JsonProperty("num_decluttered")
    private int numDecluttered;
    @JsonProperty("num_det_retried")
    private int numDetRetried;
    @JsonProperty("num_flipped")
    private int numFlipped;
    @JsonProperty("num_unread")
    private int numUnread;
    private Timings timings;

    public List<TextLine> getLines() {
        return lines;
    }

    public void setLines(List<TextLine> lines) {
        this.lines = lines;
    }

    public int getWorkW() {
        return workW;
    }

    public void setWorkW(int workW) {
        this.workW = workW;
    }

    public int getWorkH() {
        return workH;
    }

    public void setWorkH(int workH) {
        this.workH = workH;
    }

    public int getDetInputW() {
        return detInputW;
    }

    public void setDetInputW(int detInputW) {
        this.detInputW = detInputW;
    }

    public int getDetInputH() {
        return detInputH;
    }

    public void setDetInputH(int detInputH) {
        this.detInputH = detInputH;
    }

    public int getNumBoxes() {
        return numBoxes;
    }

    public void setNumBoxes(int numBoxes) {
        this.numBoxes = numBoxes;
    }

    public int getNumMerged() {
        return numMerged;
    }

    public void setNumMerged(int numMerged) {
        this.numMerged = numMerged;
    }

    public int getNumDecluttered() {
        return numDecluttered;
    }

    public void setNumDecluttered(int numDecluttered) {
        this.numDecluttered = numDecluttered;
    }

    public int getNumDetRetried() {
        return numDetRetried;
    }

    public void setNumDetRetried(int numDetRetried) {
        this.numDetRetried = numDetRetried;
    }

    public int getNumFlipped() {
        return numFlipped;
    }

    public void setNumFlipped(int numFlipped) {
        this.numFlipped = numFlipped;
    }

    public int getNumUnread() {
        return numUnread;
    }

    public void setNumUnread(int numUnread) {
        this.numUnread = numUnread;
    }

    public Timings getTimings() {
        return timings;
    }

    public void setTimings(Timings timings) {
        this.timings = timings;
    }

    /** 便捷方法：拼接全部行文本。 */
    public String plainText() {
        if (lines == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (TextLine line : lines) {
            if (line.getText() != null) {
                sb.append(line.getText()).append('\n');
            }
        }
        return sb.toString();
    }
}
