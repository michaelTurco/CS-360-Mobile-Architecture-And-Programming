package com.turco_michael_weight_tracking.ui.view_list;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        ViewListViewModel viewListViewModel =
                new ViewModelProvider(this).get(ViewListViewModel.class);

        binding = FragmentViewListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        RecyclerView recyclerView = binding.recyclerView;

        db = new UserDatabase(getContext());

        // got help from https://developer.android.com/develop/ui/views/layout/recyclerview
        // and https://www.geeksforgeeks.org/android-recyclerview/
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CustomAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        loadSQLEntries();

        return root;
    }

    private void loadSQLEntries(){
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