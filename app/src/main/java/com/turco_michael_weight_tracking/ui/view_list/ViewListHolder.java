package com.turco_michael_weight_tracking.ui.view_list;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.turco_michael_weight_tracking.R;

public class ViewListHolder extends RecyclerView.ViewHolder {
    public final TextView dateTextView;
    public final TextView weightTextView;
    public final FloatingActionButton deleteButton;

    public ViewListHolder(View view) {
        super(view);
        dateTextView = view.findViewById(R.id.entry_date);
        weightTextView = view.findViewById(R.id.entry_weight);
        deleteButton = view.findViewById(R.id.entry_delete);
    }
}
