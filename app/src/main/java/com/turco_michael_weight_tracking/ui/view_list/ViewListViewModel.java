package com.turco_michael_weight_tracking.ui.view_list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ViewListViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ViewListViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is view list fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}