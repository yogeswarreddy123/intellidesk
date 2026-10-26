package com.intellidesk.dto;

import java.util.ArrayList;
import java.util.List;

public class ChartDataDto {

    private List<String> labels = new ArrayList<>();
    private List<Long> data = new ArrayList<>();

    public ChartDataDto() {
    }

    public ChartDataDto(List<String> labels, List<Long> data) {
        this.labels = labels;
        this.data = data;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<Long> getData() {
        return data;
    }

    public void setData(List<Long> data) {
        this.data = data;
    }
}
