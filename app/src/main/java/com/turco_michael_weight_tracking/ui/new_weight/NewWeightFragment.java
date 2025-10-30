package com.turco_michael_weight_tracking.ui.new_weight;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.turco_michael_weight_tracking.LocalStorage;
import com.turco_michael_weight_tracking.R;
import com.turco_michael_weight_tracking.UserDatabase;
import com.turco_michael_weight_tracking.databinding.FragmentNewWeightBinding;

import java.util.Date;

public class NewWeightFragment extends Fragment {

    private FragmentNewWeightBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NewWeightViewModel newWeightViewModel =
                new ViewModelProvider(this).get(NewWeightViewModel.class);

        binding = FragmentNewWeightBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final Button submitButton = binding.newWeightSubmit;
        final EditText weightText = binding.newWeightField;

        submitButton.setEnabled(hasValidWeightText(weightText));

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
                submitButton.setEnabled(hasValidWeightText(weightText));
            }
        };
        weightText.addTextChangedListener(afterTextChangedListener);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasValidWeightText(weightText)) {
                    // add database entry
                    UserDatabase db = new UserDatabase(getContext());
                    db.addWeightEntry(UserDatabase.currentUserID, new Date(), getWeightFloat(weightText));


                    // check if the new weight meets the goal weight, then send a notification (if allowed)
                    boolean reachedGoal = checkGoalWeight(getWeightFloat(weightText));

                    // clear text
                    weightText.setText("");

                    // send the user to the weight history
                    if(!reachedGoal){
                        NavigateToViewList();
                    }
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private boolean hasValidWeightText(EditText text){
        return text.getText().toString().length() > 1;
    }

    private float getWeightFloat(EditText text){
        return Float.parseFloat(text.getText().toString());
    }

    private boolean checkGoalWeight(float currentWeight){
        LocalStorage storage = new LocalStorage(getContext());
        float goalWeight = storage.getGoalWeight();

        // has valid entries for both
        if(goalWeight != -1.0f && currentWeight != -1.0f){
            if (currentWeight <= goalWeight){
                // has reached goal weight successfully!
                if (hasNotificationsEnabled()){
                    // send notification that goal weight is reached
                    displayGoalReachedNotification();
                }
                return true;
            }
        }
        return false;
    }

    private boolean hasNotificationsEnabled(){
        LocalStorage storage = new LocalStorage(getContext());
        return storage.getNotificationStatus() == LocalStorage.NotificationStatus.Accepted;
    }

    private void displayGoalReachedNotification(){
        new AlertDialog.Builder(getContext())
                .setTitle("Congratulations!")
                .setMessage("Your goal weight has been reached!")
                .setPositiveButton("OK", (dialog, which) -> NavigateToHome())
                .show();
    }

    private void NavigateToHome(){
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.nav_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }

    private void NavigateToViewList(){
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.nav_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_view_list);
    }
}