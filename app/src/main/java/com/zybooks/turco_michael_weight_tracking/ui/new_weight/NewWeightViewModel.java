package com.zybooks.turco_michael_weight_tracking.ui.new_weight;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NewWeightViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public NewWeightViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is new weight fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}