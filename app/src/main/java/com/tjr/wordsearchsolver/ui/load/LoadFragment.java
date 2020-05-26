package com.tjr.wordsearchsolver.ui.load;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.tjr.wordsearchsolver.R;

public class LoadFragment extends Fragment {

    private LoadViewModel loadViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        loadViewModel =
                ViewModelProviders.of(this).get(LoadViewModel.class);
        View root = inflater.inflate(R.layout.fragment_load, container, false);

        Button loadWordsearchButton = root.findViewById(R.id.navigation_dashboard);

        return root;
    }
}