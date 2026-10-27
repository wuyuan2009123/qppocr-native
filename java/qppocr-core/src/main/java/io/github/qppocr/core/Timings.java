package io.github.qppocr.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** 分阶段毫秒计时。 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Timings {

    @JsonProperty("det_pre_ms")
    private double detPreMs;
    @JsonProperty("det_infer_ms")
    private double detInferMs;
    @JsonProperty("det_post_ms")
    private double detPostMs;
    @JsonProperty("crop_ms")
    private double cropMs;
    @JsonProperty("cls_ms")
    private double clsMs;
    @JsonProperty("rec_pre_ms")
    private double recPreMs;
    @JsonProperty("rec_infer_ms")
    private double recInferMs;
    @JsonProperty("rec_post_ms")
    private double recPostMs;
    @JsonProperty("total_ms")
    private double totalMs;

    public double getDetPreMs() {
        return detPreMs;
    }

    public void setDetPreMs(double detPreMs) {
        this.detPreMs = detPreMs;
    }

    public double getDetInferMs() {
        return detInferMs;
    }

    public void setDetInferMs(double detInferMs) {
        this.detInferMs = detInferMs;
    }

    public double getDetPostMs() {
        return detPostMs;
    }

    public void setDetPostMs(double detPostMs) {
        this.detPostMs = detPostMs;
    }

    public double getCropMs() {
        return cropMs;
    }

    public void setCropMs(double cropMs) {
        this.cropMs = cropMs;
    }

    public double getClsMs() {
        return clsMs;
    }

    public void setClsMs(double clsMs) {
        this.clsMs = clsMs;
    }

    public double getRecPreMs() {
        return recPreMs;
    }

    public void setRecPreMs(double recPreMs) {
        this.recPreMs = recPreMs;
    }

    public double getRecInferMs() {
        return recInferMs;
    }

    public void setRecInferMs(double recInferMs) {
        this.recInferMs = recInferMs;
    }

    public double getRecPostMs() {
        return recPostMs;
    }

    public void setRecPostMs(double recPostMs) {
        this.recPostMs = recPostMs;
    }

    public double getTotalMs() {
        return totalMs;
    }

    public void setTotalMs(double totalMs) {
        this.totalMs = totalMs;
    }
}
