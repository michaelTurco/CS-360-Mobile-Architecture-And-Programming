package com.turco_michael_weight_tracking.ui.login;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.google.firebase.auth.FirebaseAuth;
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
                Log.d(TAG, "signInWithEmail:success");
                authUI.onSuccessfulSignIn();
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithEmail:failure", task.getException());
                authUI.showMessage(R.string.sign_in_failed);
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
                Log.d(TAG, "createUserWithEmail:success");
                authUI.onSuccessfulSignIn();
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                authUI.showMessage(R.string.register_failed);
            }
        });

        // got help from https://firebase.google.com/docs/auth/android/start
    }
}
