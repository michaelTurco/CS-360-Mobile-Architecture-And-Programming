package com.zybooks.turco_michael_weight_tracking.data;

import android.content.Context;

import com.zybooks.turco_michael_weight_tracking.UserDatabase;
import com.zybooks.turco_michael_weight_tracking.data.model.LoggedInUser;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(Context context, String username, String password) {
        UserDatabase db = new UserDatabase(context);

        try {
            long userId = db.authenticateUser(username, password);
            LoggedInUser loggedInUser = new LoggedInUser(
                    String.valueOf(userId),
                    username
            );
            db.close();
            return new Result.Success<>(loggedInUser);

        } catch (SignInException e) {
            db.close();
            return new Result.Error(new IOException(e.getMessage()));
        } catch (Exception e) {
            db.close();
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public Result<LoggedInUser> register(Context context, String username, String password) {
        UserDatabase db = new UserDatabase(context);

        try {
            if (db.isUsernameTaken(username)) {
                throw new UsernameTakenException();
            }

            long userId = db.addUser(username, password);

            if (userId != -1) {
                LoggedInUser loggedInUser = new LoggedInUser(
                        String.valueOf(userId),
                        username
                );
                db.close();
                db.setCurrentUser(username, userId);
                return new Result.Success<>(loggedInUser);
            } else {
                throw new SignInException("Database error: Failed to create user");
            }

        } catch (SignInException e) {
            db.close();
            return new Result.Error(new IOException(e.getMessage()));
        } catch (Exception e) {
            db.close();
            return new Result.Error(new IOException("Error during registration", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }


    // got help from https://www.baeldung.com/java-new-custom-exception
    public static class SignInException extends Exception {
        public SignInException(String message) {
            super(message);
        }
    }

    public static class UsernameTakenException extends SignInException {
        public UsernameTakenException() {
            super("Username is taken");
        }
    }

    public static class PasswordIncorrectException extends SignInException {
        public PasswordIncorrectException() {
            super("Incorrect password");
        }
    }

    public static class UsernameUnknownException extends SignInException {
        public UsernameUnknownException() {
            super("Username not found");
        }
    }
}