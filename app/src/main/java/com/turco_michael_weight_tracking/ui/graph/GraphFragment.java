package com.turco_michael_weight_tracking.ui.graph;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.turco_michael_weight_tracking.LocalStorage;
import com.turco_michael_weight_tracking.NavigationUtils;
import com.turco_michael_weight_tracking.R;
import com.turco_michael_weight_tracking.databinding.FragmentGraphBinding;

public class GraphFragment extends Fragment {

    private FragmentGraphBinding binding;
    private LocalStorage storage;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentGraphBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        storage = new LocalStorage(requireContext());

        setupButtonEvents();

        return root;
    }

    private void setupButtonEvents() {
        // clicking the pencil button 'edit goal weight'
        binding.editGoalWeight.setOnClickListener(v ->
                NavigationUtils.navigateTo(this, R.id.navigation_settings)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}