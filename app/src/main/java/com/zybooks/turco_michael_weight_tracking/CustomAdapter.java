package com.zybooks.turco_michael_weight_tracking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    // got help from https://developer.android.com/develop/ui/views/layout/recyclerview
    // and https://www.geeksforgeeks.org/android-recyclerview/

    private List<WeightEntry> localDataSet;
    private final OnDeleteClickListener deleteListener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
    // date is formatted like 'Feb 21, 10:45 AM'

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView dateTextView;
        private final TextView weightTextView;
        private final FloatingActionButton deleteButton;

        public ViewHolder(View view) {
            super(view);
            dateTextView = view.findViewById(R.id.entry_date);
            weightTextView = view.findViewById(R.id.entry_weight);
            deleteButton = view.findViewById(R.id.entry_delete);
        }
    }

    public CustomAdapter(List<WeightEntry> dataSet, OnDeleteClickListener listener) {
        localDataSet = dataSet;
        deleteListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.weight_entry, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
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