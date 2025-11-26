package com.turco_michael_weight_tracking.ui.graph;

import android.content.Context;

import com.github.mikephil.charting.data.Entry;
import com.turco_michael_weight_tracking.LocalStorage;
import com.turco_michael_weight_tracking.LocalStorage.MeasurementUnit;
import com.turco_michael_weight_tracking.R;
import com.turco_michael_weight_tracking.UnitConverter;
import com.turco_michael_weight_tracking.UserDatabase;
import com.turco_michael_weight_tracking.ui.view_list.WeightEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GraphEstimation {
    public static final float UNKNOWN_TIME = -1;

    private final LocalStorage storage;
    private final MeasurementUnit unit;
    private final UserDatabase db;
    private final Context context;

    private List<WeightEntry> weightEntries;
    private List<Entry> graphWeightPoints;
    private List<Entry> graphGoalPoints;
    private List<Entry> graphEstimationPoints;
    private float goalWeight;
    private float estimatedGoalDays;

    private float minTimeSeconds;
    private float maxTimeSeconds;

    public GraphEstimation(LocalStorage storage, Context context) {
        this.storage = storage;
        this.unit = storage.getMeasurementUnit();
        this.db = UserDatabase.getInstance();
        this.context = context;

        loadDatabaseValues();
        loadGraphWeightPoints();
        loadGraphGoalPoints();
        calculateEstimationPoints();
    }

    private void loadDatabaseValues() {
        weightEntries = db.getWeightEntries();

        goalWeight = storage.getGoalWeight();
        if (goalWeight != LocalStorage.UNKNOWN) {
            goalWeight = UnitConverter.unitToUnit(goalWeight, MeasurementUnit.POUNDS, unit);
        }
    }

    private void loadGraphWeightPoints() {
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

    private void loadGraphGoalPoints() {
        graphGoalPoints = new ArrayList<>();

        // add extra space to the left and right
        float difference = (maxTimeSeconds - minTimeSeconds) + 7200;
        float extra = difference * 0.2f;

        graphGoalPoints.add(new Entry(minTimeSeconds - extra, goalWeight));
        graphGoalPoints.add(new Entry(maxTimeSeconds + extra, goalWeight));
    }

    private void calculateEstimationPoints() {
        estimatedGoalDays = UNKNOWN_TIME;
        graphEstimationPoints = null;

        // need at least 2 data points in order to calculate trend
        if (graphWeightPoints.size() < 2) return;

        graphEstimationPoints = new ArrayList<>();

        // using weighted linear regression to draw a line that fits all the points.
        // modified a bit to favor recent data entries more heavily, and older data entries less heavily
        // got help from https://en.wikipedia.org/wiki/Simple_linear_regression

        // weight decay is exponential, and uses a 7-day time scale
        float decay = 86400f * 7f;

        // weighted linear regression components
        double sumW = 0;   // sum of weights
        double sumWX = 0;  // sum of weights * x value
        double sumWY = 0;  // sum of weights * y value
        double sumWXX = 0; // sum of weights * x value squared
        double sumWXY = 0; // sum of weights * x value * y value

        // loop through entries and calculate weighted sum
        for (Entry entry : graphWeightPoints) {
            float x = entry.getX();
            float y = entry.getY();

            double w = Math.exp((x - maxTimeSeconds) / decay);

            sumW += w;
            sumWX += w * x;
            sumWY += w * y;
            sumWXX += w * x * x;
            sumWXY += w * x * y;
        }

        double denominator = (sumW * sumWXX - sumWX * sumWX);
        if (denominator == 0) return;

        // solve for linear regression coefficients in 'y = a + b*x' formula
        double a = (sumWXX * sumWY - sumWX * sumWXY) / denominator; // intercept
        double b = (sumW * sumWXY - sumWX * sumWY) / denominator;   // slope

        // replace formula variables:
        // y = a + b * x
        // goalWeight = intercept + slope * time;
        // solve for the estimated goal time
        double timeGoal = (goalWeight - a) / b;
        estimatedGoalDays = (float) ((timeGoal - maxTimeSeconds) / 86400f);

        // if the estimate says over 3 days ago, assume it is unknown instead
        if (estimatedGoalDays < -3) estimatedGoalDays = UNKNOWN_TIME;
        else if (estimatedGoalDays < 0) estimatedGoalDays = 0;

        // add extra space to the left and right
        float difference = (maxTimeSeconds - minTimeSeconds) + 7200;
        float extra = difference * 0.2f;

        graphEstimationPoints.add(new Entry(minTimeSeconds - extra, (float) (a + b * (minTimeSeconds - extra))));
        graphEstimationPoints.add(new Entry(maxTimeSeconds + extra, (float) (a + b * (maxTimeSeconds + extra))));
    }

    public List<Entry> getGraphWeightPoints() {
        return graphWeightPoints;
    }

    public List<Entry> getGraphGoalPoints() {
        return graphGoalPoints;
    }

    public List<Entry> getGraphEstimationPoints() {
        return graphEstimationPoints;
    }

    public float getGoalWeight() {
        return goalWeight;
    }

    public String getFormattedEstimatedTime() {
        if (estimatedGoalDays == UNKNOWN_TIME) {
            return context.getString(R.string.not_applicable);
        }
        if (estimatedGoalDays > 999) { // if over 3 years, don't show estimation
            return context.getString(R.string.not_applicable);
        }
        if (estimatedGoalDays > 30) { // if over a month, don't show decimal values
            return String.format(Locale.getDefault(), "%.0f days", estimatedGoalDays);
        }
        return String.format(Locale.getDefault(), "%.1f days", estimatedGoalDays);
    }
}
