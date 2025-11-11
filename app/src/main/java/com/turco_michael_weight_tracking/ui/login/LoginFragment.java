package com.turco_michael_weight_tracking.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.turco_michael_weight_tracking.LocalStorage;
import com.turco_michael_weight_tracking.MainActivity;
import com.turco_michael_weight_tracking.UserDatabase;
import com.turco_michael_weight_tracking.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private FragmentLoginBinding binding;
    private UserDatabase db;
    private LocalStorage storage;


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
        storage = new LocalStorage(requireContext());
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory()).get(LoginViewModel.class);

        setupObservers();
        setupTextWatcher();
        setupLoginListeners();

        loadAutoLoginInfo();
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
        binding.password.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handleLogin();
            }
            return false;
        });

        // handle clicking on the 'sign in' button
        binding.loginSignIn.setOnClickListener(v -> handleLogin());

        // handle clicking on the 'register' button
        binding.loginRegister.setOnClickListener(v -> handleRegister());
    }

    private void handleLogin() {
        binding.loading.setVisibility(View.VISIBLE);
        loginViewModel.login(requireContext(),
                binding.username.getText().toString(),
                binding.password.getText().toString()
        );
    }

    private void handleRegister() {
        binding.loading.setVisibility(View.VISIBLE);
        loginViewModel.register(requireContext(),
                binding.username.getText().toString(),
                binding.password.getText().toString()
        );
    }

    private void saveAutoLoginInfo() {
        // if remember me is checked, save username and password
        if (binding.rememberMe.isChecked()) {
            storage.setAutoLogin(
                    binding.username.getText().toString(),
                    binding.password.getText().toString()
            );
        }
        // not checked, so save null
        else{
            storage.setAutoLogin(null, null);
        }
    }

    private void loadAutoLoginInfo(){
        // if auto login info is stored, auto fill textboxes and checkbox, and sign in
        if (storage.getAutoLoginUsername() != null){
            binding.username.setText(storage.getAutoLoginUsername());
            binding.password.setText(storage.getAutoLoginPassword());
            binding.rememberMe.setChecked(true);
            handleLogin();
        }
    }

    private void updateUiWithUser(LoggedInUserView model) {
        // login / register was successful
        saveAutoLoginInfo();
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }


    private void showLoginFailed(@StringRes int errorString) {
        Toast.makeText(
                requireContext(),
                errorString,
                Toast.LENGTH_LONG
        ).show();
    }


    @Override
    public void onDestroyView() {
        db.close();
        binding = null;
        super.onDestroyView();
    }
}