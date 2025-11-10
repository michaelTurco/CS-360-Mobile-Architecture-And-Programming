package com.turco_michael_weight_tracking.ui.login;

import android.content.Context;
import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.turco_michael_weight_tracking.R;
import com.turco_michael_weight_tracking.data.LoginRepository;
import com.turco_michael_weight_tracking.data.Result;
import com.turco_michael_weight_tracking.data.model.LoggedInUser;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final LoginRepository loginRepository;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(Context context, String username, String password) {

        // can be launched in a separate asynchronous job
        Result<LoggedInUser> result = loginRepository.login(context, username, password);

        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
        } else {
            if (result instanceof Result.Error) {
                String error = ((Result.Error) result).getError().getMessage();

                if (error.equals("Username not found")) {
                    setSignInError(R.string.username_missing, null);
                    loginResult.setValue(new LoginResult(R.string.username_missing));
                } else if (error.equals("Incorrect password")) {
                    setSignInError(null, R.string.incorrect_password);
                    loginResult.setValue(new LoginResult(R.string.incorrect_password));
                } else {
                    loginResult.setValue(new LoginResult(R.string.sign_in_failed));
                }
            }
        }
    }

    public void register(Context context, String username, String password) {

        // can be launched in a separate asynchronous job
        Result<LoggedInUser> result = loginRepository.register(context, username, password);

        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
        } else {
            if (result instanceof Result.Error) {
                String error = ((Result.Error) result).getError().getMessage();

                if (error.equals("Username is taken")) {
                    setSignInError(R.string.username_taken, null);
                    loginResult.setValue(new LoginResult(R.string.username_taken));
                } else {
                    loginResult.setValue(new LoginResult(R.string.register_failed));
                }
            }
        }
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    public void setSignInError(Integer usernameError, Integer passwordError) {
        loginFormState.setValue(new LoginFormState(usernameError, passwordError));
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}