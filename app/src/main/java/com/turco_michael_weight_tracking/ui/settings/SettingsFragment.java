package com.turco_michael_weight_tracking.ui.settings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.turco_michael_weight_tracking.LocalStorage;
import com.turco_michael_weight_tracking.R;
import com.turco_michael_weight_tracking.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    private LocalStorage storage;

    private Button applyButton;
    private Button notificationsButton;
    private EditText newWeightText;
    private TextView notificationStatusText;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        applyButton = binding.editGoalWeightApply;
        notificationsButton = binding.toggleNotifications;
        newWeightText = binding.editGoalWeightField;
        notificationStatusText = binding.notificationsState;
        storage = new LocalStorage(requireContext());

        updateNotificationStatusText();

        applyButton.setEnabled(hasValidWeightText(newWeightText));

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyButton.setEnabled(hasValidWeightText(newWeightText));
            }
        };
        newWeightText.addTextChangedListener(afterTextChangedListener);

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasValidWeightText(newWeightText)) {
                    // save goal weight in storage
                    storage.setGoalWeight(getWeightFloat(newWeightText));

                    // clear text
                    newWeightText.setText("");

                    // return the user home
                    BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.nav_view);
                    bottomNavigationView.setSelectedItemId(R.id.navigation_home);
                }
            }
        });

        notificationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HandleNotificationButton();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private boolean hasValidWeightText(EditText text) {
        return text.getText().toString().length() > 1;
    }

    private float getWeightFloat(EditText text) {
        return Float.parseFloat(text.getText().toString());
    }

    private void updateNotificationStatusText() {
        LocalStorage.NotificationStatus status = storage.getNotificationStatus();
        if (status == LocalStorage.NotificationStatus.Accepted) {
            notificationStatusText.setText(R.string.notifications_enabled);
            notificationsButton.setText(R.string.disable_notifications);
        } else {
            notificationStatusText.setText(R.string.notifications_disabled);
            notificationsButton.setText(R.string.enable_notifications);
        }
    }

    private void HandleNotificationButton() {
        LocalStorage.NotificationStatus status = storage.getNotificationStatus();
        if (status == LocalStorage.NotificationStatus.Accepted) {
            // clicking should disable notifications
            storage.setNotificationStatus(LocalStorage.NotificationStatus.Rejected);
            updateNotificationStatusText();
        } else {
            // clicking should try to enable notifications
            displayNotificationRequest();
        }
    }

    private void NotificationsAccepted() {
        storage.setNotificationStatus(LocalStorage.NotificationStatus.Accepted);
        updateNotificationStatusText();
    }

    private void NotificationsRejected() {
        storage.setNotificationStatus(LocalStorage.NotificationStatus.Rejected);
        updateNotificationStatusText();
    }

    private void displayNotificationRequest() {
        new AlertDialog.Builder(getContext())
                .setTitle("Permission Needed")
                .setMessage("Allow this app to send notifications?")
                .setPositiveButton("Allow", (dialog, which) -> NotificationsAccepted())
                .setNegativeButton("Reject", (dialog, which) -> NotificationsRejected())
                .show();
    }
}