package com.tjr.wordsearchsolver.ui.solve;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.tjr.wordsearchsolver.R;
import com.tjr.wordsearchsolver.data.DataStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SolveFragment extends Fragment {

    private final Logger logger = LoggerFactory.getLogger(SolveFragment.class);

    private DataStore dataStore;

    private TextView loadedWordsearchText;
    private TextView loadedWordsText;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_solve, container, false);

        dataStore = DataStore.getDataStore();

        loadedWordsearchText = root.findViewById(R.id.loadedWordsearchText);
        loadedWordsText = root.findViewById(R.id.loadedWordsText);

        loadStoredValues();

        return root;
    }

    private void loadStoredValues() {
        if (dataStore.getWordsearchGrid() != null && dataStore.getWordsearchGrid().size() != 0)
            updateDisplayedWordsearch();
        if (dataStore.getSearchWords() != null && dataStore.getSearchWords().size() != 0)
            updateDisplayedWords();
    }

    private void updateDisplayedWordsearch() {
        for (List<Character> row : dataStore.getWordsearchGrid()) {
            StringBuilder rowBuilder = new StringBuilder();
            for (Character c : row)
                rowBuilder.append(c).append(" ");
            rowBuilder.append("\n");
            loadedWordsearchText.append(rowBuilder.toString());
        }
    }

    private void updateDisplayedWords() {
        StringBuilder csvBuilder = new StringBuilder();
        for (String word : dataStore.getSearchWords())
            csvBuilder.append(word).append(", ");
        String csv = csvBuilder.toString().substring(0, csvBuilder.toString().length() - 2);
        loadedWordsText.setText(csv);
    }
}