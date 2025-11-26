package com.turco_michael_weight_tracking.ui.login;

import androidx.annotation.StringRes;

public interface IAuthenticationUI {
    void onSuccessfulSignIn();

    void showMessage(@StringRes int message);

    void setLoading(boolean loading);
}
