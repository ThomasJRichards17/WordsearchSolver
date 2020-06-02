package com.tjr.wordsearchsolver.ui.solve;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.tjr.wordsearchsolver.R;
import com.tjr.wordsearchsolver.data.DataStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SolveFragment extends Fragment implements View.OnClickListener {

    private final Logger logger = LoggerFactory.getLogger(SolveFragment.class);

    private DataStore dataStore;

    private TextView wordsearchText;
    private TextView wordsText;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_solve, container, false);

        dataStore = DataStore.getDataStore();

        wordsearchText = root.findViewById(R.id.loadedWordsearchText);
        Button saveWordsearchButton = root.findViewById(R.id.saveWordsearchButton);
        saveWordsearchButton.setOnClickListener(this);
        wordsText = root.findViewById(R.id.loadedWordsText);
        Button saveWordsButton = root.findViewById(R.id.saveWordsButton);
        saveWordsButton.setOnClickListener(this);

        loadStoredValues();

        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveWordsearchButton:
                saveWordsearch();
                break;
            case R.id.saveWordsButton:
                saveWords();
                break;
        }
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
            wordsearchText.append(rowBuilder.toString());
        }
    }

    private void updateDisplayedWords() {
        StringBuilder csvBuilder = new StringBuilder();
        for (String word : dataStore.getSearchWords())
            csvBuilder.append(word).append(", ");
        String csv = csvBuilder.toString().substring(0, csvBuilder.toString().length() - 2);
        wordsText.setText(csv);
    }

    private void saveWordsearch() {
        List<List<Character>> characters = new ArrayList<>();
        String[] rows = wordsearchText.getText().toString().split("\n");
        for (String row : rows) {
            ArrayList<Character> rowAsChar = new ArrayList<>();
            String[] rowChars = row.split(" ");
            for (String c : rowChars)
                rowAsChar.add(c.charAt(0));
            characters.add(rowAsChar);
        }
        dataStore.setWordsearchGrid(characters);
    }

    private void saveWords() {
        dataStore.setSearchWords(Arrays.asList(wordsText.getText().toString().split(", ")));
    }
}