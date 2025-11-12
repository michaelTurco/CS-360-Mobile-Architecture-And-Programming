package com.turco_michael_weight_tracking.ui.view_list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.turco_michael_weight_tracking.LocalStorage;
import com.turco_michael_weight_tracking.LocalStorage.MeasurementUnit;
import com.turco_michael_weight_tracking.R;
import com.turco_michael_weight_tracking.UserDatabase;
import com.turco_michael_weight_tracking.databinding.FragmentViewListBinding;

import java.util.ArrayList;
import java.util.List;

public class ViewListFragment extends Fragment implements ViewListAdapter.OnDeleteClickListener {

    private FragmentViewListBinding binding;
    private ViewListAdapter adapter;
    private LocalStorage storage;
    private UserDatabase db;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentViewListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        storage = new LocalStorage(requireContext());
        db = new UserDatabase(getContext());

        setupButtonEvents();
        setupRecyclerView();
        loadSQLEntries();

        return root;
    }

    private void setupButtonEvents() {
        // clicking the graph button
        binding.graphButton.setOnClickListener(v -> clickGraphButton());
    }

    private void setupRecyclerView() {
        // got help from:
        // https://developer.android.com/develop/ui/views/layout/recyclerview
        // https://www.geeksforgeeks.org/android-recyclerview/

        MeasurementUnit unit = storage.getMeasurementUnit();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ViewListAdapter(new ArrayList<>(), this, requireContext(), unit);
        binding.recyclerView.setAdapter(adapter);
    }

    private void loadSQLEntries() {
        // got help from:
        // https://stackoverflow.com/questions/31367599/how-to-update-recyclerview-adapter-data

        List<WeightEntry> entries = db.getWeightEntries(UserDatabase.currentUserID);
        adapter.updateData(entries);
        adapter.notifyDataSetChanged(); // refreshes the recycler view
    }

    private void clickGraphButton() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        navController.navigate(R.id.navigation_graph);
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