package com.turco_michael_weight_tracking.ui.settings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.turco_michael_weight_tracking.LocalStorage;
import com.turco_michael_weight_tracking.LocalStorage.MeasurementUnit;
import com.turco_michael_weight_tracking.R;
import com.turco_michael_weight_tracking.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private LocalStorage storage;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        storage = new LocalStorage(requireContext());

        setupTextWatcher();
        setupButtonEvents();
        updateNotificationStatusText();
        updateMeasurementUnitSelected();

        return root;
    }

    private void setupTextWatcher() {
        // check when the goal weight text box has been edited
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // unused
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // unused
            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.editGoalWeightApply.setEnabled(hasValidWeightText(binding.editGoalWeightField));
            }
        };

        // add the listener to the text box
        binding.editGoalWeightField.addTextChangedListener(afterTextChangedListener);
    }

    private void setupButtonEvents() {
        // Goal weight 'save & apply' button
        binding.editGoalWeightApply.setOnClickListener(v -> clickGoalWeightApplyButton());

        // Toggle notifications button
        binding.toggleNotifications.setOnClickListener(v -> clickToggleNotificationsButton());

        // Measurement unit buttons
        binding.measurementPounds.setOnClickListener(v -> clickMeasurementButton(MeasurementUnit.POUNDS));
        binding.measurementKilograms.setOnClickListener(v -> clickMeasurementButton(MeasurementUnit.KILOGRAMS));
    }

    private void clickGoalWeightApplyButton() {
        if (hasValidWeightText(binding.editGoalWeightField)) {
            // save goal weight in storage
            storage.setGoalWeight(getWeightFloat(binding.editGoalWeightField));

            // clear text
            binding.editGoalWeightField.setText("");

            // return the user home
            BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.nav_view);
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }
    }

    private void clickToggleNotificationsButton() {
        LocalStorage.NotificationStatus status = storage.getNotificationStatus();
        if (status == LocalStorage.NotificationStatus.ACCEPTED) {
            // clicking should disable notifications
            storage.setNotificationStatus(LocalStorage.NotificationStatus.REJECTED);
            updateNotificationStatusText();
        } else {
            // clicking should try to enable notifications
            displayNotificationRequest();
        }
    }

    private void clickMeasurementButton(MeasurementUnit unit) {
        storage.setMeasurementUnit(unit);
        updateMeasurementUnitSelected();
    }

    private void updateMeasurementUnitSelected() {
        MeasurementUnit unit = storage.getMeasurementUnit();

        int notSelected = ContextCompat.getColor(requireContext(), R.color.gray_medium);
        int selected = ContextCompat.getColor(requireContext(), R.color.lime_medium);

        binding.measurementPounds.setBackgroundColor(notSelected);
        binding.measurementKilograms.setBackgroundColor(notSelected);

        switch (unit) {
            case POUNDS:
                binding.measurementPounds.setBackgroundColor(selected);
                break;
            case KILOGRAMS:
                binding.measurementKilograms.setBackgroundColor(selected);
                break;
        }

        // got help from https://stackoverflow.com/questions/13842447/android-set-button-background-programmatically
    }

    private boolean hasValidWeightText(EditText text) {
        return text.getText().toString().length() > 1;
    }

    private float getWeightFloat(EditText text) {
        return Float.parseFloat(text.getText().toString());
    }

    private void updateNotificationStatusText() {
        LocalStorage.NotificationStatus status = storage.getNotificationStatus();
        if (status == LocalStorage.NotificationStatus.ACCEPTED) {
            binding.notificationsState.setText(R.string.notifications_enabled);
            binding.toggleNotifications.setText(R.string.disable_notifications);
        } else {
            binding.notificationsState.setText(R.string.notifications_disabled);
            binding.toggleNotifications.setText(R.string.enable_notifications);
        }
    }

    private void NotificationsAccepted() {
        storage.setNotificationStatus(LocalStorage.NotificationStatus.ACCEPTED);
        updateNotificationStatusText();
    }

    private void NotificationsRejected() {
        storage.setNotificationStatus(LocalStorage.NotificationStatus.REJECTED);
        updateNotificationStatusText();
    }

    private void displayNotificationRequest() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.title_permission)
                .setMessage(R.string.notifications_permission)
                .setPositiveButton(R.string.permission_approve, (dialog, which) -> NotificationsAccepted())
                .setNegativeButton(R.string.permission_deny, (dialog, which) -> NotificationsRejected())
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}