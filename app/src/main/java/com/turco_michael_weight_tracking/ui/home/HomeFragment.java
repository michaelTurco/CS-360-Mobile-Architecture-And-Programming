package com.turco_michael_weight_tracking.ui.home;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.turco_michael_weight_tracking.LocalStorage;
import com.turco_michael_weight_tracking.R;
import com.turco_michael_weight_tracking.UserDatabase;
import com.turco_michael_weight_tracking.databinding.FragmentHomeBinding;

import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private LocalStorage storage;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final FloatingActionButton newWeightButton = binding.addWeight;
        final FloatingActionButton editGoalWeightButton = binding.editGoalWeight;
        final TextView welcomeText = binding.welcome;
        final TextView mostRecentWeight = binding.mostRecentWeight;
        final TextView goalWeightText = binding.goalWeight;

        storage = new LocalStorage(requireContext());

        if (UserDatabase.currentUsername != null) {
            String welcomeMessage = getString(R.string.welcome_message, UserDatabase.currentUsername);
            welcomeText.setText(welcomeMessage);
        }

        attemptNotificationRequest();


        newWeightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.nav_view);
                bottomNavigationView.setSelectedItemId(R.id.navigation_new_weight);
            }
        });

        editGoalWeightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.nav_view);
                bottomNavigationView.setSelectedItemId(R.id.navigation_settings);
            }
        });


        UserDatabase db = new UserDatabase(getContext());
        float weight = db.getMostRecentWeight(UserDatabase.currentUserID);
        if (weight == -1) {
            mostRecentWeight.setText(R.string.no_records);
        } else {
            mostRecentWeight.setText(String.format(Locale.getDefault(), "%.1f lbs", weight));
        }


        float goalWeight = storage.getGoalWeight();
        if (goalWeight == -1) {
            goalWeightText.setText(R.string.no_goal_weight);
        } else {
            goalWeightText.setText(String.format(Locale.getDefault(), "%.1f lbs", goalWeight));
        }


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void attemptNotificationRequest() {
        LocalStorage.NotificationStatus status = storage.getNotificationStatus();

        // only make request if user hasn't responded before
        if (status == LocalStorage.NotificationStatus.Unknown) {
            displayNotificationRequest();
        }
    }

    private void NotificationsAccepted() {
        storage.setNotificationStatus(LocalStorage.NotificationStatus.Accepted);
    }

    private void NotificationsRejected() {
        storage.setNotificationStatus(LocalStorage.NotificationStatus.Rejected);
    }

    private void displayNotificationRequest() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.title_permission)
                .setMessage(R.string.notifications_permission)
                .setPositiveButton(R.string.permission_approve, (dialog, which) -> NotificationsAccepted())
                .setNegativeButton(R.string.permission_deny, (dialog, which) -> NotificationsRejected())
                .show();
    }
}