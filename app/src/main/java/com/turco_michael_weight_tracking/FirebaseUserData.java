package com.turco_michael_weight_tracking;

import com.turco_michael_weight_tracking.ui.view_list.WeightEntry;

import java.util.ArrayList;
import java.util.List;

public class FirebaseUserData {
    public List<WeightEntry> weightEntries;

    public FirebaseUserData() {
        weightEntries = new ArrayList<>();
    }
}
