package com.turco_michael_weight_tracking;

import android.app.Activity;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;

public class NavigationUtils {
    public static void navigateTo(Activity activity, @IdRes int menu) {
        ((MainActivity) activity).navigateToMenu(menu);
    }

    public static void navigateTo(Fragment fragment, @IdRes int menu) {
        ((MainActivity) fragment.requireActivity()).navigateToMenu(menu);
    }
}
