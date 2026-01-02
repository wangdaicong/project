package com.example.bicolorsphere.domain;

import java.time.LocalDate;
import java.util.List;

public class SsqDraw {
    private String drawNo;
    private LocalDate drawDate;
    private List<Integer> reds;
    private int blue;

    public SsqDraw(String drawNo, LocalDate drawDate, List<Integer> reds, int blue) {
        this.drawNo = drawNo;
        this.drawDate = drawDate;
        this.reds = reds;
        this.blue = blue;
    }

    public String getDrawNo() {
        return drawNo;
    }

    public LocalDate getDrawDate() {
        return drawDate;
    }

    public List<Integer> getReds() {
        return reds;
    }

    public int getBlue() {
        return blue;
    }
}
