package com.turco_michael_weight_tracking.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.turco_michael_weight_tracking.MainActivity;
import com.turco_michael_weight_tracking.UserDatabase;
import com.turco_michael_weight_tracking.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private FragmentLoginBinding binding;
    private UserDatabase db;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new UserDatabase(requireContext());
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory()).get(LoginViewModel.class);

        setupObservers();
        setupTextWatcher();
        setupLoginListeners();
    }

    private void setupObservers() {
        // handle when a change is made to the login form state
        loginViewModel.getLoginFormState().observe(getViewLifecycleOwner(), formState -> {
            if (formState == null) return;

            binding.loginSignIn.setEnabled(formState.isDataValid());
            binding.loginRegister.setEnabled(formState.isDataValid());

            if (formState.getUsernameError() != null)
                binding.username.setError(getString(formState.getUsernameError()));

            if (formState.getPasswordError() != null)
                binding.password.setError(getString(formState.getPasswordError()));
        });

        // handle when a login result is made
        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
            if (loginResult == null) return;

            binding.loading.setVisibility(View.GONE);

            if (loginResult.getError() != null)
                showLoginFailed(loginResult.getError());
            else if (loginResult.getSuccess() != null)
                updateUiWithUser(loginResult.getSuccess());
        });
    }

    private void setupTextWatcher() {
        // check when either of the text boxes have been edited
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
                // send textbox contents to login view model
                loginViewModel.loginDataChanged(
                        binding.username.getText().toString(),
                        binding.password.getText().toString()
                );
            }
        };

        // add the listeners to the 2 text boxes
        binding.username.addTextChangedListener(afterTextChangedListener);
        binding.password.addTextChangedListener(afterTextChangedListener);
    }

    private void setupLoginListeners() {
        // handle when 'enter' is pressed while inside the password text box
        binding.password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    handleLogin();
                }
                return false;
            }
        });

        // handle clicking on the 'sign in' button
        binding.loginSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        // handle clicking on the 'register' button
        binding.loginRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegister();
            }
        });
    }

    private void handleLogin() {
        binding.loading.setVisibility(View.VISIBLE);
        loginViewModel.login(requireContext(),
                binding.username.getText().toString(),
                binding.password.getText().toString());
    }

    private void handleRegister() {
        binding.loading.setVisibility(View.VISIBLE);
        loginViewModel.register(requireContext(),
                binding.username.getText().toString(),
                binding.password.getText().toString());
    }

    private void updateUiWithUser(LoggedInUserView model) {
        // successful login !!
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }


    private void showLoginFailed(@StringRes Integer errorString) {
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(
                    getContext().getApplicationContext(),
                    errorString,
                    Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onDestroyView() {
        db.close();
        super.onDestroyView();
        binding = null;
    }
}