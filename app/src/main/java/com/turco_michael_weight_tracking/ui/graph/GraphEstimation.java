package com.turco_michael_weight_tracking.ui.graph;

import com.github.mikephil.charting.data.Entry;
import com.turco_michael_weight_tracking.LocalStorage;
import com.turco_michael_weight_tracking.LocalStorage.MeasurementUnit;
import com.turco_michael_weight_tracking.UnitConverter;
import com.turco_michael_weight_tracking.UserDatabase;
import com.turco_michael_weight_tracking.ui.view_list.WeightEntry;

import java.util.ArrayList;
import java.util.List;

public class GraphEstimation {
    public static final float UNKNOWN_TIME = -1;

    private final LocalStorage storage;
    private final MeasurementUnit unit;
    private final UserDatabase db;

    private List<Entry> graphWeightPoints;

    public float minTimeSeconds;
    public float maxTimeSeconds;

    public GraphEstimation(LocalStorage storage, UserDatabase db) {
        this.storage = storage;
        this.unit = storage.getMeasurementUnit();
        this.db = db;

        loadGraphWeightPoints();
    }

    private void loadGraphWeightPoints() {
        List<WeightEntry> weightEntries = db.getWeightEntries(UserDatabase.currentUserID);
        graphWeightPoints = new ArrayList<>();

        minTimeSeconds = Float.MAX_VALUE;
        maxTimeSeconds = 0;

        for (WeightEntry entry : weightEntries) {
            float timeSec = entry.getDate().getTime() / 1000f;

            // store min and max of graph timestamp data
            if (minTimeSeconds > timeSec) minTimeSeconds = timeSec;
            if (maxTimeSeconds < timeSec) maxTimeSeconds = timeSec;

            float weight = UnitConverter.unitToUnit(entry.getWeight(), MeasurementUnit.POUNDS, unit);

            graphWeightPoints.add(new Entry(timeSec, weight));
        }

        // if too few entries, set the min and max within 4 hours of right now
        if (weightEntries.size() < 2) {
            float timeSec = System.currentTimeMillis() / 1000f;
            minTimeSeconds = timeSec - 7200;
            maxTimeSeconds = timeSec + 7200;
        }

        // sort the list by x value
        graphWeightPoints.sort((a, b) -> Float.compare(a.getX(), b.getX()));
    }

    public List<Entry> getGraphWeightPoints() {
        return graphWeightPoints;
    }
}
