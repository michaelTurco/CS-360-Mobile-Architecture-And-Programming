package com.turco_michael_weight_tracking.ui.view_list;

import java.util.Date;

public class WeightEntry {
    private final Date date;
    private final float weight;

    public WeightEntry() {
        this.date = new Date();
        this.weight = 0;
    }

    public WeightEntry(Date date, float weight) {
        this.date = date;
        this.weight = weight;
    }

    // Getters
    public Date getDate() {
        return date;
    }

    public float getWeight() {
        return weight;
    }
}