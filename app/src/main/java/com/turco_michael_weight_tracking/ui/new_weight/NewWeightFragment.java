package com.turco_michael_weight_tracking.ui.new_weight;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.turco_michael_weight_tracking.LocalStorage;
import com.turco_michael_weight_tracking.LocalStorage.MeasurementUnit;
import com.turco_michael_weight_tracking.R;
import com.turco_michael_weight_tracking.UnitConverter;
import com.turco_michael_weight_tracking.UserDatabase;
import com.turco_michael_weight_tracking.databinding.FragmentNewWeightBinding;

import java.util.Date;

public class NewWeightFragment extends Fragment {

    private FragmentNewWeightBinding binding;
    private LocalStorage storage;
    private MeasurementUnit measurementUnit;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNewWeightBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        storage = new LocalStorage(requireContext());
        measurementUnit = storage.getMeasurementUnit();

        setupTextWatcher();
        setupButtonEvents();
        setupMenuTexts();
        updateSubmitButtonEnabled();

        return root;
    }

    private void setupTextWatcher() {
        // check when the new weight text box has been edited
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
                updateSubmitButtonEnabled();
            }
        };

        // add the listener to the text box
        binding.newWeightField.addTextChangedListener(afterTextChangedListener);
    }

    private void setupButtonEvents() {
        // click 'submit' button
        binding.newWeightSubmit.setOnClickListener(v -> clickSubmitButton());
    }

    private void setupMenuTexts() {
        // set the texts in the menu related to the custom measurement unit
        String LongMeasurementText = UnitConverter.getLongUnitString(requireContext(), measurementUnit);

        String descriptionMessage = getString(R.string.new_weight_description, LongMeasurementText);
        binding.descriptionNewWeight.setText(descriptionMessage);

        String newWeightHint = getString(R.string.new_weight_hint, LongMeasurementText);
        binding.newWeightField.setHint(newWeightHint);
    }

    private void clickSubmitButton() {
        if (hasValidWeightText(binding.newWeightField)) {
            // add database entry
            UserDatabase db = new UserDatabase(getContext());
            db.addWeightEntry(UserDatabase.currentUserID, new Date(), getWeightFloat(binding.newWeightField));

            // check if the new weight meets the goal weight, then send a notification (if allowed)
            boolean reachedGoal = checkGoalWeight(getWeightFloat(binding.newWeightField));

            // clear text
            binding.newWeightField.setText("");

            // send the user to the weight history
            if (!reachedGoal) {
                NavigateToViewList();
            }
        }
    }

    private void updateSubmitButtonEnabled() {
        binding.newWeightSubmit.setEnabled(hasValidWeightText(binding.newWeightField));
    }

    private boolean hasValidWeightText(EditText text) {
        return text.getText().toString().length() > 1;
    }

    private float getWeightFloat(EditText text) {
        // convert input into pounds based on what measurement unit they are currently using
        float input = Float.parseFloat(text.getText().toString());
        return UnitConverter.unitToPounds(input, measurementUnit);
    }

    private boolean checkGoalWeight(float currentWeight) {
        float goalWeight = storage.getGoalWeight();

        // has valid entries for both
        if (goalWeight != LocalStorage.UNKNOWN && currentWeight != LocalStorage.UNKNOWN) {
            if (currentWeight <= goalWeight) {
                // has reached goal weight successfully!
                if (hasNotificationsEnabled()) {
                    // send notification that goal weight is reached
                    displayGoalReachedNotification();
                }
                return true;
            }
        }
        return false;
    }

    private boolean hasNotificationsEnabled() {
        return storage.getNotificationStatus() == LocalStorage.NotificationStatus.ACCEPTED;
    }

    private void displayGoalReachedNotification() {
        new AlertDialog.Builder(getContext())
                .setTitle("Congratulations!")
                .setMessage("Your goal weight has been reached!")
                .setPositiveButton("OK", (dialog, which) -> NavigateToHome())
                .show();
    }

    private void NavigateToHome() {
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.nav_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }

    private void NavigateToViewList() {
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.nav_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_view_list);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}