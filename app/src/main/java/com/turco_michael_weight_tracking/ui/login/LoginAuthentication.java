package com.turco_michael_weight_tracking.ui.login;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.turco_michael_weight_tracking.R;

public class LoginAuthentication {
    private static final String ACCOUNT_EMAIL_ENDING = "@local.app";

    private final FirebaseAuth auth;
    private final FragmentActivity activity;
    private final IAuthenticationUI authUI;

    public LoginAuthentication(FragmentActivity activity, IAuthenticationUI authUI) {
        this.activity = activity;
        this.authUI = authUI;

        // Initialize Firebase auth
        auth = FirebaseAuth.getInstance();
    }

    public void signIn(String username, String password) {
        String email = username + ACCOUNT_EMAIL_ENDING;

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(activity, task -> {
            authUI.setLoading(false);

            if (task.isSuccessful()) {
                // Sign in success!
                authUI.onSuccessfulSignIn();
            } else {
                // If sign in fails, display a message to the user.
                authUI.showMessage(getExceptionMessage(task.getException()));
            }
        });

        // got help from https://firebase.google.com/docs/auth/android/start
    }

    public void register(String username, String password) {
        String email = username + ACCOUNT_EMAIL_ENDING;

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(activity, task -> {
            authUI.setLoading(false);

            if (task.isSuccessful()) {
                // Sign in success!
                authUI.onSuccessfulSignIn();
            } else {
                // If sign in fails, display a message to the user.
                authUI.showMessage(getExceptionMessage(task.getException()));
            }
        });

        // got help from https://firebase.google.com/docs/auth/android/start
    }

    private int getExceptionMessage(Exception e) {
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return R.string.auth_error_invalid_credentials;
        }

        if (e instanceof FirebaseAuthUserCollisionException) {
            return R.string.auth_error_email_in_use;
        }

        if (e instanceof FirebaseNetworkException) {
            return R.string.auth_error_network;
        }

        return R.string.auth_error_unknown;

        // got exception ideas from https://firebase.google.com/docs/reference/android/com/google/firebase/FirebaseException
    }
}
