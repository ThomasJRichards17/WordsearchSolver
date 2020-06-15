package com.tjr.wordsearchsolver.ui.solve;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.tjr.wordsearchsolver.R;
import com.tjr.wordsearchsolver.data.Coordinate;
import com.tjr.wordsearchsolver.data.DataStore;
import com.tjr.wordsearchsolver.data.FoundWord;
import com.tjr.wordsearchsolver.data.Solution;
import com.tjr.wordsearchsolver.processing.WordsearchProcessor;
import com.tjr.wordsearchsolver.utils.WordsearchUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

public class SolveFragment extends Fragment implements View.OnClickListener {

    private Logger logger = LoggerFactory.getLogger(SolveFragment.class);

    private ReentrantLock lock = new ReentrantLock();
    private DataStore dataStore;
    private WordsearchProcessor wordsearchProcessor;
    private WordsearchUtils wordsearchUtils;

    private TextView wordsearchText;
    private TextView wordsearchSavedText;
    private TextView wordsText;
    private TextView wordsSavedText;
    private TextView foundWordsText;

    private TableLayout solvedWordsearchGrid;

    private boolean wordsearchSaved;
    private boolean wordsSaved;
    private String fileName;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_solve, container, false);

        dataStore = DataStore.getDataStore();
        wordsearchProcessor = new WordsearchProcessor();
        wordsearchUtils = new WordsearchUtils();

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
        foundWordsText = root.findViewById(R.id.solvedWordsText);
        Button saveSolutionButton = root.findViewById(R.id.saveSolutionButton);
        saveSolutionButton.setOnClickListener(this);

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
            case R.id.saveSolutionButton:
                saveSolution();
                break;
        }
    }

    private void loadStoredValues() {
        boolean hasGrid;
        if (dataStore.getWordsearchGrid() != null && dataStore.getWordsearchGrid().size() != 0) {
            updateDisplayedWordsearch();
            setWordsearchSavedText(true);
            wordsearchSaved = true;
            hasGrid = true;
        } else {
            wordsearchSavedText.setVisibility(View.GONE);
            wordsearchSaved = false;
            hasGrid = false;
        }

        if (dataStore.getSearchWords() != null && dataStore.getSearchWords().size() != 0) {
            updateDisplayedWords();
            setWordsSavedText(true);
            wordsSaved = true;
        } else {
            wordsSavedText.setVisibility(View.GONE);
            wordsSaved = false;
        }

        solvedWordsearchGrid.removeAllViews();

        if (hasGrid && dataStore.getFoundWords() != null && dataStore.getFoundWords().size() != 0) {
            drawSolvedWordsearchGrid();
            highlightFoundWords(dataStore.getFoundWords());
            updateFoundWordsText();
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
        if (!wordsearchText.getText().toString().isEmpty() && !wordsearchSaved) {
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

            Snackbar wordsearchSaved = Snackbar.make(requireActivity().findViewById(R.id.navigation_solve), "Wordsearch successfully saved!", Snackbar.LENGTH_SHORT);
            wordsearchSaved.setBackgroundTint(Color.parseColor("#228B22"));
            wordsearchSaved.show();
        }
    }

    private void saveWords() {
        if (!wordsText.getText().toString().isEmpty() && !wordsSaved) {
            String[] splitWords = wordsText.getText().toString().split(", ");
            List<String> words = new ArrayList<>();
            Collections.addAll(words, splitWords);
            dataStore.setSearchWords(words);
            setWordsSavedText(true);
            wordsSaved = true;

            Snackbar wordsSaved = Snackbar.make(requireActivity().findViewById(R.id.navigation_solve), "Words list successfully saved!", Snackbar.LENGTH_SHORT);
            wordsSaved.setBackgroundTint(Color.parseColor("#228B22"));
            wordsSaved.show();
        }
    }

    private void saveSolution() {
        if (solvedWordsearchGrid.getChildCount() != 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Save Wordsearch Solution");
            final EditText input = new EditText(requireActivity().getApplicationContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setHint("Please enter the name of the solution:");
            builder.setView(input);
            builder.setPositiveButton("Save", (dialog, which) -> {
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                fileName = input.getText().toString();
                if (TextUtils.isEmpty(fileName))
                    return;
                writeSolutionToFile();
                dialog.dismiss();
            });
        } else {
            Snackbar solutionNotSaved = Snackbar.make(requireActivity().findViewById(R.id.navigation_solve), "Solution can't be saved - please solve a wordsearch first!", Snackbar.LENGTH_SHORT);
            solutionNotSaved.setBackgroundTint(Color.parseColor("#B22222"));
            solutionNotSaved.show();
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

    private void writeSolutionToFile() {
        Gson gson = new Gson();
        List<Solution> savedSolutions = new ArrayList<>();
        String fileName = "saved_solutions.json";
        boolean fileExists = false;

        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File solutionsFile = new File(storageDir, fileName);
        if (solutionsFile.exists())
            fileExists = true;

        setColoursToFoundWords();

        try {
            if (fileExists)
                savedSolutions = gson.fromJson(new JsonReader(new FileReader(solutionsFile)), new TypeToken<ArrayList<Solution>>() {
                }.getType());

            Solution solutionToWrite = new Solution(fileName, dataStore.getFoundWords(), dataStore.getWordsearchGrid());
            savedSolutions.add(solutionToWrite);

            try (FileWriter writer = new FileWriter(solutionsFile)) {
                gson.toJson(savedSolutions, writer);
                writer.flush();
            }

            Snackbar solutionSaved = Snackbar.make(requireActivity().findViewById(R.id.navigation_solve), "Solution saved! It can be loaded from the Solutions tab.", Snackbar.LENGTH_SHORT);
            solutionSaved.setBackgroundTint(Color.parseColor("#228B22"));
            solutionSaved.show();
        } catch (IOException e) {
            logger.error("Error saving wordsearch solution - ", e);
        }
    }

    private void solveWordsearch() {
        long startTime = System.currentTimeMillis();
        long stopTime;
        if (wordsearchSaved && wordsSaved) {
            char[][] board = new char[dataStore.getWordsearchGrid().size()][];

            int i = 0;
            for (List<Character> list : dataStore.getWordsearchGrid()) {
                char[] array = new char[list.size()];
                for (int j = 0; j < list.size(); j++)
                    array[j] = Character.toLowerCase(list.get(j));
                board[i++] = array;
            }

            List<String> searchWords = dataStore.getSearchWords();
            Future<List<FoundWord>> foundWordsFuture = null;
            lock.lock();
            try {
                foundWordsFuture = wordsearchProcessor.findWords(board, new ArrayList<>(searchWords));
            } finally {
                lock.unlock();
                if (foundWordsFuture != null) {
                    try {
                        List<FoundWord> foundWords = foundWordsFuture.get();
                        dataStore.setFoundWords(foundWords);
                        solvedWordsearchGrid.removeAllViews();
                        drawSolvedWordsearchGrid();
                        highlightFoundWords(foundWords);
                        stopTime = System.currentTimeMillis();
                        Snackbar wordsearchSolved = Snackbar.make(requireActivity().findViewById(R.id.navigation_solve),
                                MessageFormat.format("{0}/{1} words were found in {2} seconds - scroll down to view the solutions",
                                        foundWords.size() > searchWords.size() ? searchWords.size() : new HashSet<>(foundWords).size(),
                                        searchWords.size(), ((double) (stopTime - startTime)) / 1000), Snackbar.LENGTH_SHORT);
                        wordsearchSolved.setBackgroundTint(Color.parseColor("#228B22"));
                        wordsearchSolved.show();
                        updateFoundWordsText();
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
            text.setSingleLine();
            text.setTextColor(Color.BLACK);
            text.setGravity(Gravity.CENTER);
            row.addView(text);
        }
        return row;
    }

    private void highlightFoundWords(List<FoundWord> foundWords) {
        int colour = 0;
        boolean colourFound = false;
        HashMap<Coordinate, Integer> colourMapping = new HashMap<>();
        List<List<Coordinate>> coordinatesLists = new ArrayList<>();
        for (FoundWord foundWord : foundWords)
            coordinatesLists.add(wordsearchUtils.getCoordinatesBetweenPoints(foundWord.start, foundWord.end));
        for (List<Coordinate> coordinates : coordinatesLists) {
            for (Coordinate coordinate : coordinates) {
                for (Coordinate coord : colourMapping.keySet()) {
                    if (coord.equals(coordinate)) {
                        Integer fromMap = colourMapping.get(coord);
                        if (fromMap != null) {
                            colourFound = true;
                            colour = fromMap;
                            break;
                        }
                    }
                }
            }

            if (!colourFound) {
                while (!colourFound) {
                    colour = wordsearchUtils.getRandomColour();
                    if (ColorUtils.calculateLuminance(colour) < 0.85 && ColorUtils.calculateLuminance(colour) > 0.35)
                        colourFound = true;
                }
                colourFound = false;
            }
            for (Coordinate coordinate : coordinates) {
                TableRow row = (TableRow) solvedWordsearchGrid.getChildAt(coordinate.x);
                TextView text = (TextView) row.getChildAt(coordinate.y);
                text.setBackgroundColor(colour);
                colourMapping.put(coordinate, colour);
            }
        }
    }

    private void setColoursToFoundWords() {
        for (FoundWord foundWord : dataStore.getFoundWords()) {
            TableRow row = (TableRow) solvedWordsearchGrid.getChildAt(foundWord.start.x);
            foundWord.wordColour = ((ColorDrawable) row.getChildAt(foundWord.start.y).getBackground()).getColor();
        }
    }

    private void updateFoundWordsText() {
        StringBuilder builder = new StringBuilder();
        for (FoundWord foundWord : dataStore.getFoundWords())
            builder.append(foundWord.toString()).append("\n");
        foundWordsText.setText(builder.toString().trim());
    }
}