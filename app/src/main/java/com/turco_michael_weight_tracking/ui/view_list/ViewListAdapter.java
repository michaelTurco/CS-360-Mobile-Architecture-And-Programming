package com.turco_michael_weight_tracking.ui.view_list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.turco_michael_weight_tracking.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ViewListAdapter extends RecyclerView.Adapter<ViewListHolder> {

    // got help from https://developer.android.com/develop/ui/views/layout/recyclerview
    // and https://www.geeksforgeeks.org/android-recyclerview/

    private List<WeightEntry> localDataSet;
    private final OnDeleteClickListener deleteListener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
    // date is formatted like 'Feb 21, 10:45 AM'

    public ViewListAdapter(List<WeightEntry> dataSet, OnDeleteClickListener listener) {
        localDataSet = dataSet;
        deleteListener = listener;
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
        WeightEntry entry = localDataSet.get(position);

        // Format date and weight
        String formattedDate = dateFormat.format(entry.getDate());
        String formattedWeight = String.format(Locale.getDefault(), "%.1f lbs", entry.getWeight());

        viewHolder.dateTextView.setText(formattedDate);
        viewHolder.weightTextView.setText(formattedWeight);

        viewHolder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) { // fix crash
                deleteListener.onDeleteClick(entry.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public void updateData(List<WeightEntry> newData) {
        localDataSet = newData;
    }

    // got help from https://stackoverflow.com/questions/49969278/recyclerview-item-click-listener-the-right-way
    // to implement the recycler view's click listener
    public interface OnDeleteClickListener {
        void onDeleteClick(long entryId);
    }
}