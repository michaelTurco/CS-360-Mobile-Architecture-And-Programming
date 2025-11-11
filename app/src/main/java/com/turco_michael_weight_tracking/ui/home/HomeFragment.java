package com.turco_michael_weight_tracking.ui.home;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.turco_michael_weight_tracking.LocalStorage;
import com.turco_michael_weight_tracking.LocalStorage.MeasurementUnit;
import com.turco_michael_weight_tracking.R;
import com.turco_michael_weight_tracking.UnitConverter;
import com.turco_michael_weight_tracking.UserDatabase;
import com.turco_michael_weight_tracking.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private LocalStorage storage;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        storage = new LocalStorage(requireContext());

        setupButtonEvents();
        setWelcomeText();
        updateDisplayValues();
        attemptNotificationRequest();

        return root;
    }

    private void setupButtonEvents() {
        // clicking the plus button 'add new weight'
        binding.addWeight.setOnClickListener(v -> navigateToMenu(R.id.navigation_new_weight));

        // clicking the pencil button 'edit goal weight'
        binding.editGoalWeight.setOnClickListener(v -> navigateToMenu(R.id.navigation_settings));
    }

    private void navigateToMenu(@IdRes int menu) {
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.nav_view);
        bottomNavigationView.setSelectedItemId(menu);
    }

    private void setWelcomeText() {
        if (UserDatabase.currentUsername != null) {
            String welcomeMessage = getString(R.string.welcome_message, UserDatabase.currentUsername);
            binding.welcome.setText(welcomeMessage);
        }
    }

    private void updateDisplayValues() {
        UserDatabase db = new UserDatabase(getContext());
        MeasurementUnit unit = storage.getMeasurementUnit();

        // update 'most recent weight'
        float weight = db.getMostRecentWeight(UserDatabase.currentUserID);
        if (weight == LocalStorage.UNKNOWN) {
            binding.mostRecentWeight.setText(R.string.no_records);
        } else {
            binding.mostRecentWeight.setText(UnitConverter.poundsToFormattedUnitString(requireContext(), weight, unit));
        }

        // update 'goal weight'
        float goalWeight = storage.getGoalWeight();
        if (goalWeight == LocalStorage.UNKNOWN) {
            binding.goalWeight.setText(R.string.no_goal_weight);
        } else {
            binding.goalWeight.setText(UnitConverter.poundsToFormattedUnitString(requireContext(), goalWeight, unit));
        }
    }

    private void attemptNotificationRequest() {
        LocalStorage.NotificationStatus status = storage.getNotificationStatus();

        // only make request if user hasn't responded before
        if (status == LocalStorage.NotificationStatus.UNKNOWN) {
            displayNotificationRequest();
        }
    }

    private void NotificationsAccepted() {
        storage.setNotificationStatus(LocalStorage.NotificationStatus.ACCEPTED);
    }

    private void NotificationsRejected() {
        storage.setNotificationStatus(LocalStorage.NotificationStatus.REJECTED);
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