package com.turco_michael_weight_tracking.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.turco_michael_weight_tracking.LocalStorage;
import com.turco_michael_weight_tracking.databinding.FragmentAccountBinding;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private LocalStorage storage;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        storage = new LocalStorage(requireContext());

        setupButtonEvents();
        updateDisplayValues();

        return root;
    }

    private void setupButtonEvents() {
        // clicking the 'sign out' button
    }

    private void updateDisplayValues() {
        // update 'account nickname'
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}