package com.turco_michael_weight_tracking;

import android.app.Activity;

import androidx.annotation.IdRes;

public class NavigationUtils {
    public static void navigateTo(Activity activity, @IdRes int menu) {
        ((MainActivity) activity).navigateToMenu(menu);
    }
}
