package com.tjr.wordsearchsolver.ui.solve;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.tjr.wordsearchsolver.R;
import com.tjr.wordsearchsolver.data.DataStore;
import com.tjr.wordsearchsolver.data.FoundWord;
import com.tjr.wordsearchsolver.processing.WordsearchProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

public class SolveFragment extends Fragment implements View.OnClickListener {

    private Logger logger = LoggerFactory.getLogger(SolveFragment.class);

    private ReentrantLock lock = new ReentrantLock();
    private DataStore dataStore;
    private WordsearchProcessor wordsearchProcessor;

    private TextView wordsearchText;
    private TextView wordsearchSavedText;
    private TextView wordsText;
    private TextView wordsSavedText;

    private TableLayout solvedWordsearchGrid;

    private boolean wordsearchSaved;
    private boolean wordsSaved;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_solve, container, false);

        dataStore = DataStore.getDataStore();
        wordsearchProcessor = new WordsearchProcessor();

        wordsearchText = root.findViewById(R.id.loadedWordsearchText);
        wordsearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setWordsearchSavedText(false);
                wordsearchSaved = false;
            }
        });
        Button saveWordsearchButton = root.findViewById(R.id.saveWordsearchButton);
        saveWordsearchButton.setOnClickListener(this);
        wordsearchSavedText = root.findViewById(R.id.wordsearchSavedText);

        wordsText = root.findViewById(R.id.loadedWordsText);
        wordsText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setWordsSavedText(false);
                wordsSaved = false;
            }
        });
        Button saveWordsButton = root.findViewById(R.id.saveWordsButton);
        saveWordsButton.setOnClickListener(this);
        wordsSavedText = root.findViewById(R.id.wordsSavedText);

        Button solveButton = root.findViewById(R.id.solveWordsearchButton);
        solveButton.setOnClickListener(this);

        solvedWordsearchGrid = root.findViewById(R.id.solvedWordsearchGrid);

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
            case R.id.solveWordsearchButton:
                solveWordsearch();
                break;
        }
    }

    private void loadStoredValues() {
        if (dataStore.getWordsearchGrid() != null && dataStore.getWordsearchGrid().size() != 0) {
            updateDisplayedWordsearch();
            setWordsearchSavedText(true);
            wordsearchSaved = true;
        } else {
            wordsearchSavedText.setVisibility(View.GONE);
            wordsearchSaved = false;
        }

        if (dataStore.getSearchWords() != null && dataStore.getSearchWords().size() != 0) {
            updateDisplayedWords();
            setWordsSavedText(true);
            wordsSaved = true;
        } else {
            wordsSavedText.setVisibility(View.GONE);
            wordsSaved = false;
        }
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
        if (!wordsearchText.getText().toString().isEmpty()) {
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
            setWordsearchSavedText(true);
            wordsearchSaved = true;
        }
    }

    private void saveWords() {
        if (!wordsText.getText().toString().isEmpty()) {
            String[] splitWords = wordsText.getText().toString().split(", ");
            List<String> words = new ArrayList<>();
            Collections.addAll(words, splitWords);
            dataStore.setSearchWords(words);
            setWordsSavedText(true);
            wordsSaved = true;
        }
    }

    private void setWordsearchSavedText(boolean saved) {
        wordsearchSavedText.setVisibility(View.VISIBLE);
        if (saved)
            wordsearchSavedText.setText(requireActivity().getResources().getString(R.string.wordsearch_saved_text));
        else
            wordsearchSavedText.setText(requireActivity().getResources().getString(R.string.wordsearch_unsaved_text));
    }

    private void setWordsSavedText(boolean saved) {
        wordsSavedText.setVisibility(View.VISIBLE);
        if (saved)
            wordsSavedText.setText(requireActivity().getResources().getString(R.string.words_saved_text));
        else
            wordsSavedText.setText(requireActivity().getResources().getString(R.string.words_unsaved_text));
    }

    private void solveWordsearch() {
        if (wordsearchSaved && wordsSaved) {
            char[][] board = new char[dataStore.getWordsearchGrid().size()][];

            int i = 0;
            for (List<Character> list : dataStore.getWordsearchGrid()) {
                char[] array = new char[list.size()];
                for (int j = 0; j < list.size(); j++)
                    array[j] = Character.toLowerCase(list.get(j));
                board[i++] = array;
            }

            Future<List<FoundWord>> foundWordsFuture = null;

            lock.lock();
            try {
                foundWordsFuture = wordsearchProcessor.findWords(board, dataStore.getSearchWords());
            } finally {
                lock.unlock();
                if (foundWordsFuture != null) {
                    try {
                        String s = " ";
                        for (FoundWord foundWord : foundWordsFuture.get())
                            System.out.println(foundWord.toString() + "\n");
                        drawSolvedWordsearchGrid();
                    } catch (ExecutionException | InterruptedException e) {
                        logger.error("Error getting found words", e);
                    }
                }
            }
        } else {
            Snackbar mustBeSaved = Snackbar.make(requireActivity().findViewById(R.id.navigation_solve), requireActivity().getResources().getString(R.string.must_be_saved_text), Snackbar.LENGTH_SHORT);
            mustBeSaved.show();
        }
    }

    private void drawSolvedWordsearchGrid() {
        for (int i = 0; i < dataStore.getWordsearchGrid().size(); i++)
            solvedWordsearchGrid.addView(buildEmptyRow(dataStore.getWordsearchGrid().get(i)));
    }

    private TableRow buildEmptyRow(List<Character> chars) {
        TableRow row = new TableRow(requireActivity().getApplicationContext());
        for (int i = 0; i < dataStore.getWordsearchGrid().get(0).size(); i++) {
            TextView text = new TextView(requireActivity().getApplicationContext());
            text.setText(String.valueOf(chars.get(i)));
            text.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            text.setBackground(requireActivity().getDrawable(R.drawable.border));
            text.setSingleLine();
            text.setTextColor(Color.BLACK);
            text.setGravity(Gravity.CENTER);
            row.addView(text);
        }
        return row;
    }
}