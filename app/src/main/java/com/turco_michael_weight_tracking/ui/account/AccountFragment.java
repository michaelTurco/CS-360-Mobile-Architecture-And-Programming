package com.turco_michael_weight_tracking.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.turco_michael_weight_tracking.LocalStorage;
import com.turco_michael_weight_tracking.NavigationUtils;
import com.turco_michael_weight_tracking.R;
import com.turco_michael_weight_tracking.UserDatabase_OLD;
import com.turco_michael_weight_tracking.databinding.FragmentAccountBinding;
import com.turco_michael_weight_tracking.ui.login.LoginActivity;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private LocalStorage storage;
    private UserDatabase_OLD db;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        storage = new LocalStorage(requireContext());
        db = new UserDatabase_OLD(requireContext());

        setupButtonEvents();
        updateDisplayValues();
        setupTextWatcher();
        updateSaveButtonEnabled();

        return root;
    }

    private void setupButtonEvents() {
        // Goal weight 'save & apply' button
        binding.editNicknameApply.setOnClickListener(v -> clickNicknameApplyButton());

        // clicking the 'sign out' button
        binding.signOutButton.setOnClickListener(v -> clickSignOutButton());
    }

    private void updateDisplayValues() {
        // update 'account nickname'
        String nickname = storage.getAccountNickname();

        if (UserDatabase_OLD.currentUsername != null) {
            // set nickname to account username if it isn't set yet
            if (nickname == null) {
                nickname = UserDatabase_OLD.currentUsername;
                storage.setAccountNickname(nickname);
            }

            binding.editNicknameField.setText(nickname);
        }
    }

    private void setupTextWatcher() {
        // check when the nickname text box has been edited
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
                updateSaveButtonEnabled();
            }
        };

        // add the listener to the text box
        binding.editNicknameField.addTextChangedListener(afterTextChangedListener);
    }

    private void updateSaveButtonEnabled() {
        binding.editNicknameApply.setEnabled(hasValidNicknameText());

        if (binding.editNicknameApply.isEnabled()) {
            int enabledColor = ContextCompat.getColor(requireContext(), R.color.blue_medium);
            binding.editNicknameApply.setBackgroundColor(enabledColor);
            binding.editNicknameApply.setAlpha(1.0f);
        } else {
            int disabledColor = ContextCompat.getColor(requireContext(), R.color.transparent_75);
            binding.editNicknameApply.setBackgroundColor(disabledColor);
            binding.editNicknameApply.setAlpha(0.5f);
        }
    }

    private boolean hasValidNicknameText() {
        // account name is between 2 and 16 inclusive
        int length = binding.editNicknameField.getText().toString().length();
        return length >= 2 && length <= 16;
    }

    private void clickNicknameApplyButton() {
        // cancel if the input is invalid
        if (!hasValidNicknameText()) return;

        // save nickname in storage
        String nickname = binding.editNicknameField.getText().toString();
        storage.setAccountNickname(nickname);

        // send the user to the home page
        NavigationUtils.navigateTo(this, R.id.navigation_home);
    }

    private void clickSignOutButton() {
        // clear any auto-login information
        storage.setAutoLogin(null, null);

        // 'sign out' of current account
        db.setCurrentUser(null, 0);

        // switch to login activity
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}