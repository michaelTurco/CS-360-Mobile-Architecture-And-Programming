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
import com.turco_michael_weight_tracking.LocalStorage;
import com.turco_michael_weight_tracking.LocalStorage.MeasurementUnit;
import com.turco_michael_weight_tracking.NavigationUtils;
import com.turco_michael_weight_tracking.R;
import com.turco_michael_weight_tracking.UnitConverter;
import com.turco_michael_weight_tracking.databinding.FragmentGraphBinding;

import java.util.ArrayList;
import java.util.List;

public class GraphFragment extends Fragment {

    private FragmentGraphBinding binding;
    private LocalStorage storage;
    private MeasurementUnit unit;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentGraphBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        storage = new LocalStorage(requireContext());
        unit = storage.getMeasurementUnit();

        setupButtonEvents();
        setupGraph();
        updateDisplayValues();

        return root;
    }

    private void setupButtonEvents() {
        // clicking the pencil button 'edit goal weight'
        binding.editGoalWeight.setOnClickListener(v -> NavigationUtils.navigateTo(this, R.id.navigation_settings));
    }

    private void setupGraph() {
        // set up color variables
        int weightLineColor = ContextCompat.getColor(requireContext(), R.color.graph_weight_line);
        int circleColor = ContextCompat.getColor(requireContext(), R.color.graph_circle);
        int innerCircleColor = ContextCompat.getColor(requireContext(), R.color.graph_circle_hole);
        int goalWeightLineColor = ContextCompat.getColor(requireContext(), R.color.graph_goal_weight_line);

        // set up chart properties
        LineChart chart = binding.graph;
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setPinchZoom(true);
        chart.setScaleEnabled(true);
        chart.getAxisRight().setEnabled(false);

        // set up X axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setGridLineWidth(1f);
        xAxis.setTextSize(14f);

        // set up Y axis
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setDrawGridLines(true);
        yAxis.setGridLineWidth(1f);
        yAxis.setTextSize(14f);

        // set up the keys that show up on the bottom of the graph
        Legend legend = chart.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setXEntrySpace(15f);
        legend.setFormSize(14f);
        legend.setTextSize(14f);
        legend.setEnabled(true);

        // temp entries just for testing
        List<Entry> weightEntries = new ArrayList<>();
        weightEntries.add(new Entry(1, 180));
        weightEntries.add(new Entry(2, 177));
        weightEntries.add(new Entry(3, 176));
        weightEntries.add(new Entry(4, 172));

        // set up weight points and line style
        LineDataSet weightDataSet = new LineDataSet(weightEntries, "Weight");
        weightDataSet.setColor(weightLineColor);
        weightDataSet.setLineWidth(4f);
        weightDataSet.setCircleColor(circleColor);
        weightDataSet.setCircleRadius(12f);
        weightDataSet.setCircleHoleColor(innerCircleColor);
        weightDataSet.setCircleHoleRadius(8f);
        weightDataSet.setDrawValues(false);

        // set up the goal weight values
        float goalWeight = storage.getGoalWeight();
        if (goalWeight != LocalStorage.UNKNOWN) {
            // convert to local units and add to graph
            goalWeight = UnitConverter.unitToPounds(goalWeight, unit);
            List<Entry> goalEntries = new ArrayList<>();
            goalEntries.add(new Entry(0.9f, goalWeight));
            goalEntries.add(new Entry(4.1f, goalWeight));

            // set up goal weight line settings, dashed line that is straight
            LineDataSet goalDataSet = new LineDataSet(goalEntries, "Goal");
            goalDataSet.setColor(goalWeightLineColor);
            goalDataSet.setLineWidth(4f);
            goalDataSet.enableDashedLine(20f, 15f, 0f);
            goalDataSet.setDrawCircles(false);
            goalDataSet.setDrawValues(false);

            // add the data and the goal line to the chart to display
            LineData data = new LineData(weightDataSet, goalDataSet);
            chart.setData(data);
        } else {
            // only add the data to the chart to display
            LineData data = new LineData(weightDataSet);
            chart.setData(data);
        }

        // cause the graph to redraw
        chart.invalidate();

        // got help from:
        // https://www.geeksforgeeks.org/android/point-graph-series-in-android/
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}