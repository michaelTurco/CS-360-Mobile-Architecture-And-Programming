package com.turco_michael_weight_tracking.ui.view_list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.turco_michael_weight_tracking.LocalStorage.MeasurementUnit;
import com.turco_michael_weight_tracking.R;
import com.turco_michael_weight_tracking.UnitConverter;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

// got help from:
// https://developer.android.com/develop/ui/views/layout/recyclerview
// https://www.geeksforgeeks.org/android-recyclerview/
public class ViewListAdapter extends RecyclerView.Adapter<ViewListHolder> {
    private final OnDeleteClickListener deleteListener;
    private final Context context;
    private final MeasurementUnit measurementUnit;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
    // date is formatted like 'Feb 21, 10:45 AM'

    private List<WeightEntry> dataSet;

    public ViewListAdapter(List<WeightEntry> dataSet, OnDeleteClickListener deleteListener, Context context, MeasurementUnit measurementUnit) {
        this.dataSet = dataSet;
        this.deleteListener = deleteListener;
        this.context = context;
        this.measurementUnit = measurementUnit;
    }

    @NonNull
    @Override
    public ViewListHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.weight_entry, viewGroup, false);
        return new ViewListHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewListHolder viewHolder, int position) {
        WeightEntry entry = dataSet.get(position);

        // Format date and weight
        String formattedDate = dateFormat.format(entry.getDate());
        String formattedWeight = UnitConverter.poundsToFormattedUnitString(context, entry.getWeight(), measurementUnit);

        viewHolder.dateTextView.setText(formattedDate);
        viewHolder.weightTextView.setText(formattedWeight);

        viewHolder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) { // fix crash
                deleteListener.onDeleteClick(entry);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void updateData(List<WeightEntry> newData) {
        dataSet = newData;
    }

    // got help from:
    // https://stackoverflow.com/questions/49969278/recyclerview-item-click-listener-the-right-way
    // to implement the recycler view's click listener
    public interface OnDeleteClickListener {
        void onDeleteClick(WeightEntry entry);
    }
}