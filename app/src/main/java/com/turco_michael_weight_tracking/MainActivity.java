package com.turco_michael_weight_tracking;

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.turco_michael_weight_tracking.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        setupNavigationBar();
        setupNavigationListener();
    }

    private void setupNavigationBar() {
        // add the 5 main buttons to the navigation bar
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_view_list,
                R.id.navigation_new_weight,
                R.id.navigation_settings,
                R.id.navigation_account
        ).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    private void setupNavigationListener() {
        BottomNavigationView navView = findViewById(R.id.nav_view);

        // handle when a navigation button is pressed
        navView.setOnItemSelectedListener(item -> {
            navigateToMenu(item.getItemId());
            return true;
        });
    }

    public void navigateToMenu(@IdRes int menuId) {
        // setup default transition animations
        NavOptions navOptions = new NavOptions.Builder()
                .setEnterAnim(androidx.navigation.ui.R.anim.nav_default_enter_anim)
                .setExitAnim(androidx.navigation.ui.R.anim.nav_default_exit_anim)
                .setPopEnterAnim(androidx.navigation.ui.R.anim.nav_default_pop_enter_anim)
                .setPopExitAnim(androidx.navigation.ui.R.anim.nav_default_pop_exit_anim)
                .build();

        navController.popBackStack(menuId, false);

        // only navigate if it is a different menu
        if (navController.getCurrentDestination() == null || navController.getCurrentDestination().getId() != menuId) {
            navController.navigate(menuId, null, navOptions);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // handle the back arrow button press
        return navController.navigateUp() || super.onSupportNavigateUp();
        // found some help from:
        // https://developer.android.com/guide/navigation/integrations/ui
    }
}