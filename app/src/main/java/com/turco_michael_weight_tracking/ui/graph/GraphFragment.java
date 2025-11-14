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
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
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
    private List<ILineDataSet> graphLines;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentGraphBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        storage = new LocalStorage(requireContext());
        unit = storage.getMeasurementUnit();
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

        // set up X axis
        XAxis xAxis = graph.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setGridLineWidth(1f);
        xAxis.setTextSize(14f);

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

        // add this graph line to the list of lines
        graphLines.add(weightDataSet);
    }

    private void setupGraphGoalWeightLine() {
        // check if a valid goal weight is set
        float goalWeight = storage.getGoalWeight();
        if (goalWeight == LocalStorage.UNKNOWN) return;

        // set up color variables
        int goalWeightLineColor = ContextCompat.getColor(requireContext(), R.color.graph_goal_weight_line);

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

        // add this graph line to the list of lines
        graphLines.add(goalDataSet);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}