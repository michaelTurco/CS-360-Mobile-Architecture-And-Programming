package com.turco_michael_weight_tracking.ui.view_list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.turco_michael_weight_tracking.CustomAdapter;
import com.turco_michael_weight_tracking.UserDatabase;
import com.turco_michael_weight_tracking.WeightEntry;
import com.turco_michael_weight_tracking.databinding.FragmentViewListBinding;

import java.util.ArrayList;
import java.util.List;

public class ViewListFragment extends Fragment implements CustomAdapter.OnDeleteClickListener {

    private FragmentViewListBinding binding;
    private CustomAdapter adapter;
    private UserDatabase db;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentViewListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = new UserDatabase(getContext());

        setupRecyclerView();
        loadSQLEntries();

        return root;
    }

    private void setupRecyclerView() {
        // got help from https://developer.android.com/develop/ui/views/layout/recyclerview
        // and https://www.geeksforgeeks.org/android-recyclerview/
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CustomAdapter(new ArrayList<>(), this);
        binding.recyclerView.setAdapter(adapter);
    }

    private void loadSQLEntries() {
        List<WeightEntry> entries = db.getWeightEntries(UserDatabase.currentUserID);
        adapter.updateData(entries);
        adapter.notifyDataSetChanged(); // refreshes the recycler view
        // https://stackoverflow.com/questions/31367599/how-to-update-recyclerview-adapter-data
    }

    @Override
    public void onDeleteClick(long entryId) {
        if (db.deleteWeightEntry(UserDatabase.currentUserID, entryId)) {
            // Refresh the list after deletion
            loadSQLEntries();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}