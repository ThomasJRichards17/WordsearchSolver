package com.tjr.wordsearchsolver.ui.solve;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

class SolveViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public SolveViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is solve fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}