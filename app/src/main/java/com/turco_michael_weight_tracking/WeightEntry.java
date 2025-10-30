package com.turco_michael_weight_tracking;

import java.util.Date;

public class WeightEntry {
    private final long id;
    private final Date date;
    private final float weight;

    public WeightEntry(long id, Date date, float weight) {
        this.id = id;
        this.date = date;
        this.weight = weight;
    }

    // Getters
    public long getId() { return id; }
    public Date getDate() { return date; }
    public float getWeight() { return weight; }
}