package com.turco_michael_weight_tracking.ui.view_list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.turco_michael_weight_tracking.LocalStorage;
import com.turco_michael_weight_tracking.LocalStorage.MeasurementUnit;
import com.turco_michael_weight_tracking.NavigationUtils;
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
        db = UserDatabase.getInstance();

        setupButtonEvents();
        setupRecyclerView();
        loadSQLEntries();

        return root;
    }

    private void setupButtonEvents() {
        // clicking the graph button
        binding.graphButton.setOnClickListener(v -> NavigationUtils.navigateTo(this, R.id.navigation_graph));
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

        List<WeightEntry> entries = db.getWeightEntries();
        adapter.updateData(entries);
        adapter.notifyDataSetChanged(); // refreshes the recycler view
    }

    @Override
    public void onDeleteClick(WeightEntry entry) {
        setLoading(true);

        db.deleteWeightEntry(entry, this::deleteWeightEntryCallback);
    }

    private void deleteWeightEntryCallback(boolean result) {
        setLoading(false);

        // if data written to firebase successfully
        if (result) {
            // Refresh the list after deletion
            loadSQLEntries();
            return;
        }

        // error, unable to save data
        showMessage(R.string.delete_error);
    }

    public void showMessage(@StringRes int message) {
        // display a small message on the bottom of the screen,
        // usually an error that something wasn't able to save
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    public void setLoading(boolean loading) {
        if (loading) {
            binding.loading.setVisibility(View.VISIBLE);
        } else {
            binding.loading.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}