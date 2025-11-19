package com.turco_michael_weight_tracking.ui.graph;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.turco_michael_weight_tracking.LocalStorage;
import com.turco_michael_weight_tracking.LocalStorage.MeasurementUnit;
import com.turco_michael_weight_tracking.NavigationUtils;
import com.turco_michael_weight_tracking.R;
import com.turco_michael_weight_tracking.UnitConverter;
import com.turco_michael_weight_tracking.UserDatabase;
import com.turco_michael_weight_tracking.databinding.FragmentGraphBinding;
import com.turco_michael_weight_tracking.ui.view_list.WeightEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GraphFragment extends Fragment {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("M/d", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    private FragmentGraphBinding binding;
    private LocalStorage storage;
    private MeasurementUnit unit;
    private UserDatabase db;
    private List<ILineDataSet> graphLines;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentGraphBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        storage = new LocalStorage(requireContext());
        unit = storage.getMeasurementUnit();
        db = new UserDatabase(getContext());
        graphLines = new ArrayList<>();

        setupButtonEvents();
        updateDisplayValues();

        setupGraphSettings();
        setupGraphWeightLine();
        setupGraphGoalWeightLine();
        renderGraph();

        return root;
    }

    private void setupButtonEvents() {
        // clicking the pencil button 'edit goal weight'
        binding.editGoalWeight.setOnClickListener(v -> NavigationUtils.navigateTo(this, R.id.navigation_settings));

        // clicking the reset graph button
        binding.resetGraph.setOnClickListener(v -> resetGraph());
    }

    private void updateDisplayValues() {
        // update 'goal weight'
        float goalWeight = storage.getGoalWeight();
        if (goalWeight == LocalStorage.UNKNOWN) {
            binding.goalWeight.setText(R.string.no_goal_weight);
        } else {
            binding.goalWeight.setText(UnitConverter.poundsToFormattedUnitString(requireContext(), goalWeight, unit));
        }
    }

    private void setupGraphSettings() {
        // set up chart properties
        LineChart graph = binding.graph;
        graph.getDescription().setEnabled(false);
        graph.setTouchEnabled(true);
        graph.setPinchZoom(true);
        graph.setScaleEnabled(true);
        graph.getAxisRight().setEnabled(false);
        graph.setDragDecelerationEnabled(false);

        // set up X axis
        XAxis xAxis = graph.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setGridLineWidth(1f);
        xAxis.setTextSize(14f);
        xAxis.setLabelCount(4);

        // set up x axis formatter
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return formatDayValue(value);
            }
        });

        // set up Y axis
        YAxis yAxis = graph.getAxisLeft();
        yAxis.setDrawGridLines(true);
        yAxis.setGridLineWidth(1f);
        yAxis.setTextSize(14f);

        // set up the keys that show up on the bottom of the graph
        Legend legend = graph.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setXEntrySpace(15f);
        legend.setFormSize(14f);
        legend.setTextSize(14f);
        legend.setEnabled(true);

        // got help from:
        // https://www.geeksforgeeks.org/android/point-graph-series-in-android/
    }

    private void setupGraphWeightLine() {
        // set up color variables
        int weightLineColor = ContextCompat.getColor(requireContext(), R.color.graph_weight_line);
        int circleColor = ContextCompat.getColor(requireContext(), R.color.graph_circle);
        int innerCircleColor = ContextCompat.getColor(requireContext(), R.color.graph_circle_hole);

        // load weight data points
        List<Entry> weightEntries = loadGraphWeightPoints();

        // set up weight points and line style
        LineDataSet weightDataSet = new LineDataSet(weightEntries, "Weight");
        weightDataSet.setColor(weightLineColor);
        weightDataSet.setLineWidth(4f);
        weightDataSet.setCircleColor(circleColor);
        weightDataSet.setCircleRadius(12f);
        weightDataSet.setCircleHoleColor(innerCircleColor);
        weightDataSet.setCircleHoleRadius(8f);
        weightDataSet.setDrawValues(false);

        // add this graph line to the list of lines
        graphLines.add(weightDataSet);
    }

    private List<Entry> loadGraphWeightPoints() {
        List<Entry> graphPoints = new ArrayList<>();
        List<WeightEntry> weightEntries = db.getWeightEntries(UserDatabase.currentUserID);

        for (WeightEntry entry : weightEntries) {
            long timeMS = entry.getDate().getTime();
            float timeSec = timeMS / 1000f;

            float weight = entry.getWeight();
            weight = UnitConverter.unitToUnit(weight, MeasurementUnit.POUNDS, unit);

            graphPoints.add(new Entry(timeSec, weight));
        }

        // sort the list by x value
        graphPoints.sort((a, b) -> Float.compare(a.getX(), b.getX()));

        return graphPoints;
    }

    private void setupGraphGoalWeightLine() {
        // check if a valid goal weight is set
        float goalWeight = storage.getGoalWeight();
        if (goalWeight == LocalStorage.UNKNOWN) return;

        // set up color variables
        int goalWeightLineColor = ContextCompat.getColor(requireContext(), R.color.graph_goal_weight_line);

        // convert to local units and add to graph
        goalWeight = UnitConverter.unitToUnit(goalWeight, MeasurementUnit.POUNDS, unit);
        List<Entry> goalEntries = loadGraphGoalLine(goalWeight);

        // set up goal weight line settings, dashed line that is straight
        LineDataSet goalDataSet = new LineDataSet(goalEntries, "Goal");
        goalDataSet.setColor(goalWeightLineColor);
        goalDataSet.setLineWidth(4f);
        goalDataSet.enableDashedLine(20f, 15f, 0f);
        goalDataSet.setDrawCircles(false);
        goalDataSet.setDrawValues(false);

        // add this graph line to the list of lines
        graphLines.add(goalDataSet);
    }

    private List<Entry> loadGraphGoalLine(float goalWeight) {
        List<Entry> graphPoints = new ArrayList<>();
        List<WeightEntry> weightEntries = db.getWeightEntries(UserDatabase.currentUserID);

        float minimumSec = Float.MAX_VALUE;
        float maximumSec = 0;

        for (WeightEntry entry : weightEntries) {
            long timeMS = entry.getDate().getTime();
            float timeSec = timeMS / 1000f;

            if (maximumSec < timeSec) maximumSec = timeSec;
            if (minimumSec > timeSec) minimumSec = timeSec;
        }

        // if too few entries, put the graph around today
        if (weightEntries.size() < 2) {
            long timeMS = System.currentTimeMillis();
            float timeSec = timeMS / 1000f;
            minimumSec = timeSec - 7200;
            maximumSec = timeSec + 7200;
        }

        float difference = (maximumSec - minimumSec) + 3600;
        float extraDistance = difference * 0.1f;

        graphPoints.add(new Entry(minimumSec - extraDistance, goalWeight));
        graphPoints.add(new Entry(maximumSec + extraDistance, goalWeight));

        return graphPoints;
    }

    private void renderGraph() {
        LineChart graph = binding.graph;

        // add all the graph lines to the graph
        LineData data = new LineData(graphLines);
        graph.setData(data);

        // cause the graph to redraw
        graph.invalidate();
    }

    private void resetGraph() {
        LineChart graph = binding.graph;

        // re center and align the graph position
        graph.fitScreen();

        // cause the graph to redraw
        graph.invalidate();
    }

    private String formatDayValue(float value) {
        long timeMS = (long) value * 1000L;
        float range = binding.graph.getVisibleXRange();

        if (range > 76800) {
            return dateFormat.format(new Date(timeMS));
        } else {
            return timeFormat.format(new Date(timeMS));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}