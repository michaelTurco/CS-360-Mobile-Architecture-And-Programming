package com.turco_michael_weight_tracking.ui.login;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.turco_michael_weight_tracking.LocalStorage;
import com.turco_michael_weight_tracking.MainActivity;
import com.turco_michael_weight_tracking.R;
import com.turco_michael_weight_tracking.UserDatabase;
import com.turco_michael_weight_tracking.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private UserDatabase db;
    private LocalStorage storage;
    private FirebaseAuth mAuth;

    private boolean hasValidUsername;
    private boolean hasValidPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new UserDatabase(requireContext());
        storage = new LocalStorage(requireContext());

        initializeFirebaseAuth();
        setupTextWatcher();
        setupButtonEvents();
        loadAutoLoginInfo();
    }

    private void initializeFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void setupTextWatcher() {
        // check when the username text box has been edited
        TextWatcher usernameChangedListener = new TextWatcher() {
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
                onUsernameFieldChanged(binding.username.getText().toString());
            }
        };

        // check when the password text box has been edited
        TextWatcher passwordChangedListener = new TextWatcher() {
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
                onPasswordFieldChanged(binding.password.getText().toString());
            }
        };

        // add the listeners to the 2 text boxes
        binding.username.addTextChangedListener(usernameChangedListener);
        binding.password.addTextChangedListener(passwordChangedListener);
    }

    private void onUsernameFieldChanged(String text) {
        hasValidUsername = false;

        if (text.isEmpty()) {
            binding.username.setError(null);
        } else if (text.length() < 5) {
            binding.username.setError(getText(R.string.username_too_short));
        } else {
            binding.username.setError(null);
            hasValidUsername = true;
        }

        updateButtonsEnabled();
    }

    private void onPasswordFieldChanged(String text) {
        hasValidPassword = false;

        if (text.isEmpty()) {
            binding.password.setError(null);
        } else if (text.length() < 5) {
            binding.password.setError(getText(R.string.password_too_short));
        } else {
            binding.password.setError(null);
            hasValidPassword = true;
        }

        updateButtonsEnabled();
    }

    private void updateButtonsEnabled() {
        boolean enabled = hasValidPassword && hasValidUsername;

        binding.loginSignIn.setEnabled(enabled);
        binding.loginRegister.setEnabled(enabled);
    }

    private void setupButtonEvents() {
        // handle clicking on the 'sign in' button
        binding.loginSignIn.setOnClickListener(v -> clickSignInButton());

        // handle clicking on the 'register' button
        binding.loginRegister.setOnClickListener(v -> clickRegisterButton());

        // handle when 'enter' is pressed while inside the password text box
        binding.password.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                clickSignInButton();
            }
            return false;
        });
    }

    private void clickSignInButton() {
        setLoading(true);

        String username = binding.username.getText().toString();
        String password = binding.password.getText().toString();
        signIn(username, password);
    }

    private void clickRegisterButton() {
        setLoading(true);

        String username = binding.username.getText().toString();
        String password = binding.password.getText().toString();
        register(username, password);
    }

    private void loadAutoLoginInfo() {
        // if auto login info is stored, auto fill textboxes and checkbox, and sign in
        if (storage.getAutoLoginUsername() != null) {
            binding.username.setText(storage.getAutoLoginUsername());
            binding.password.setText(storage.getAutoLoginPassword());
            binding.rememberMe.setChecked(true);
            clickSignInButton();
        }
    }

    private void saveAutoLoginInfo() {
        // if remember me is checked, save username and password
        if (binding.rememberMe.isChecked()) {
            String username = binding.username.getText().toString();
            String password = binding.password.getText().toString();

            storage.setAutoLogin(username, password);
        }
        // not checked, so save null
        else {
            storage.setAutoLogin(null, null);
        }
    }

    private void signIn(String username, String password) {
        String email = username + "@local.app";

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity(), task -> {
            setLoading(false);

            if (task.isSuccessful()) {
                // Sign in success!
                Log.d(TAG, "signInWithEmail:success");
                onSuccessfulSignIn();
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithEmail:failure", task.getException());
                showToast(R.string.sign_in_failed);
            }
        });
    }

    private void register(String username, String password) {
        String email = username + "@local.app";

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity(), task -> {
            setLoading(false);

            if (task.isSuccessful()) {
                // Sign in success!
                Log.d(TAG, "createUserWithEmail:success");
                onSuccessfulSignIn();
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                showToast(R.string.register_failed);
            }
        });
    }

    private void onSuccessfulSignIn() {
        saveAutoLoginInfo();

        // switch to main activity
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showToast(@StringRes int message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    private void setLoading(boolean loading) {
        if (loading) {
            binding.loading.setVisibility(View.VISIBLE);
        } else {
            binding.loading.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        db.close();
        binding = null;
        super.onDestroyView();
    }
}