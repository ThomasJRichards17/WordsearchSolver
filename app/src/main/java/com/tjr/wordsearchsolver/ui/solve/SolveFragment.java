package com.tjr.wordsearchsolver.ui.solve;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.CheckBox;
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

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT;

public class SolveFragment extends Fragment implements View.OnClickListener {

    private final Logger logger = LoggerFactory.getLogger(SolveFragment.class);

    private final ReentrantLock lock = new ReentrantLock();
    private DataStore dataStore;
    private WordsearchProcessor wordsearchProcessor;
    private WordsearchUtils wordsearchUtils;

    private TextView wordsearchText;
    private TextView wordsearchSavedText;
    private TextView wordsText;
    private TextView wordsSavedText;
    private TextView solutionHeading;

    private TableLayout solvedWordsearchGrid;
    private TableLayout solvedWordsGrid;

    private Button saveSolutionButton;

    private boolean wordsearchSaved;
    private boolean wordsSaved;

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

        solutionHeading = root.findViewById(R.id.solutionHeading);

        solvedWordsearchGrid = root.findViewById(R.id.solvedWordsearchGrid);
        solvedWordsGrid = root.findViewById(R.id.solvedWordsGrid);

        saveSolutionButton = root.findViewById(R.id.saveSolutionButton);
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
            default:
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
            solutionHeading.setVisibility(View.VISIBLE);
            drawSolvedWordsearchGrid();
            highlightFoundWords(dataStore.getFoundWords());
            drawFoundWordsGrid(dataStore.getFoundWords());
            saveSolutionButton.setVisibility(View.VISIBLE);
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

            Snackbar wordsearchSaved = Snackbar.make(requireActivity().findViewById(R.id.navigation_solve), "Wordsearch successfully saved!", LENGTH_SHORT);
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

            Snackbar wordsSaved = Snackbar.make(requireActivity().findViewById(R.id.navigation_solve), "Words list successfully saved!", LENGTH_SHORT);
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
                String solutionName = input.getText().toString();
                if (TextUtils.isEmpty(solutionName))
                    return;
                if (writeSolutionToFile(solutionName)) {
                    dialog.dismiss();
                } else {
                    Snackbar nameTaken = Snackbar.make(requireActivity().findViewById(R.id.navigation_solve), "A solution already exists with this name - please try again with a different name!", LENGTH_SHORT);
                    nameTaken.setBackgroundTint(Color.parseColor("#B22222"));
                    nameTaken.show();
                }
            });
        } else {
            Snackbar solutionNotSaved = Snackbar.make(requireActivity().findViewById(R.id.navigation_solve), "Solution can't be saved - please solve a wordsearch first!", LENGTH_SHORT);
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

    private boolean writeSolutionToFile(String solutionName) {
        Gson gson = new Gson();
        List<Solution> savedSolutions = new ArrayList<>();
        String fileName = "saved_solutions.json";
        boolean fileExists = false;

        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File solutionsFile = new File(storageDir, fileName);
        if (solutionsFile.exists())
            fileExists = true;

        try {
            if (fileExists) {
                savedSolutions = gson.fromJson(new JsonReader(new FileReader(solutionsFile)), new TypeToken<ArrayList<Solution>>() {
                }.getType());
                for (Solution solution : savedSolutions) {
                    if (solution.name.equals(solutionName))
                        return false;
                }
            }

            Solution solutionToWrite = new Solution(solutionName, dataStore.getFoundWords(), dataStore.getWordsearchGrid());
            savedSolutions.add(solutionToWrite);

            try (FileWriter writer = new FileWriter(solutionsFile)) {
                gson.toJson(savedSolutions, writer);
                writer.flush();
            }

            Snackbar solutionSaved = Snackbar.make(requireActivity().findViewById(R.id.navigation_solve), "Solution saved! It can be loaded from the Solutions tab.", LENGTH_SHORT);
            solutionSaved.setBackgroundTint(Color.parseColor("#228B22"));
            solutionSaved.show();

            return true;
        } catch (IOException e) {
            logger.error("Error saving wordsearch solution - ", e);
            return false;
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
                        Collections.sort(foundWords, (o1, o2) -> o1.word.compareTo(o2.word));
                        getWordColours(foundWords);
                        dataStore.setFoundWords(foundWords);
                        solvedWordsearchGrid.removeAllViews();
                        solutionHeading.setVisibility(View.VISIBLE);
                        drawSolvedWordsearchGrid();
                        highlightFoundWords(foundWords);
                        drawFoundWordsGrid(foundWords);
                        saveSolutionButton.setVisibility(View.VISIBLE);
                        stopTime = System.currentTimeMillis();
                        Snackbar wordsearchSolved = Snackbar.make(requireActivity().findViewById(R.id.navigation_solve),
                                MessageFormat.format("{0}/{1} words were found in {2} seconds - scroll down to view the solutions",
                                        foundWords.size() > searchWords.size() ? searchWords.size() : new HashSet<>(foundWords).size(),
                                        searchWords.size(), ((double) (stopTime - startTime)) / 1000), LENGTH_SHORT);
                        wordsearchSolved.setBackgroundTint(Color.parseColor("#228B22"));
                        wordsearchSolved.show();
                    } catch (ExecutionException | InterruptedException e) {
                        logger.error("Error getting found words", e);
                    }
                }
            }
        } else {
            Snackbar mustBeSaved = Snackbar.make(requireActivity().findViewById(R.id.navigation_solve), requireActivity().getResources().getString(R.string.must_be_saved_text), LENGTH_SHORT);
            mustBeSaved.show();
        }
    }

    private void drawSolvedWordsearchGrid() {
        for (int i = 0; i < dataStore.getWordsearchGrid().size(); i++)
            solvedWordsearchGrid.addView(buildEmptyRow(dataStore.getWordsearchGrid().get(i)));
    }

    private TableRow buildEmptyRow(List<Character> chars) {
        TableRow row = new TableRow(requireActivity().getApplicationContext());
        for (int i = 0; i < chars.size(); i++) {
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
        for (FoundWord foundWord : foundWords) {
            List<Coordinate> coordinates = wordsearchUtils.getCoordinatesBetweenPoints(foundWord.start, foundWord.end);
            for (Coordinate coordinate : coordinates) {
                TableRow row = (TableRow) solvedWordsearchGrid.getChildAt(coordinate.x);
                TextView text = (TextView) row.getChildAt(coordinate.y);
                text.setBackgroundColor(foundWord.wordColour);
            }
        }
    }

    private void getWordColours(List<FoundWord> foundWords) {
        HashMap<Coordinate, Integer> coordinateMap = new HashMap<>();
        List<List<Coordinate>> allCoordinates = new ArrayList<>();

        for (FoundWord word : foundWords) {
            List<Coordinate> coordinates = wordsearchUtils.getCoordinatesBetweenPoints(word.start, word.end);
            allCoordinates.add(coordinates);
            for (Coordinate coordinate : coordinates) {
                if (coordinateMap.containsKey(coordinate)) {
                    Integer value = coordinateMap.get(coordinate);
                    if (value != null)
                        coordinateMap.put(coordinate, value + 1);
                } else {
                    coordinateMap.put(coordinate, 1);
                }
            }
        }

        for (FoundWord word : foundWords) {
            if (word.wordColour == 0) {
                List<Coordinate> coordinates = wordsearchUtils.getCoordinatesBetweenPoints(word.start, word.end);
                for (Coordinate coordinate : coordinates) {
                    Integer value = coordinateMap.get(coordinate);
                    if (value != null && value > 1) {
                        for (int i = 0; i < allCoordinates.size(); i++) {
                            List<Coordinate> coords = allCoordinates.get(i);
                            if (!coords.equals(coordinates)) {
                                if (coords.contains(coordinate))
                                    word.wordColour = foundWords.get(i).wordColour;
                            }
                        }
                    }

                    if (word.wordColour == 0) {
                        boolean colourFound = false;
                        while (!colourFound) {
                            int colour = wordsearchUtils.getRandomColour();
                            if (ColorUtils.calculateLuminance(colour) < 0.85 && ColorUtils.calculateLuminance(colour) > 0.30) {
                                word.wordColour = colour;
                                colourFound = true;
                            }
                        }
                    }
                }
            }
        }
    }

    private void drawFoundWordsGrid(List<FoundWord> foundWords) {
        solvedWordsGrid.removeAllViews();
        solvedWordsGrid.addView(buildHeadingRow());

        List<List<Coordinate>> coordinatesLists = new ArrayList<>();
        for (FoundWord foundWord : foundWords)
            coordinatesLists.add(wordsearchUtils.getCoordinatesBetweenPoints(foundWord.start, foundWord.end));

        HashMap<Coordinate, Integer> highlightMap = new HashMap<>();
        for (List<Coordinate> l : coordinatesLists) {
            for (Coordinate c : l) {
                if (highlightMap.containsKey(c)) {
                    Integer value = highlightMap.get(c);
                    if (value != null)
                        highlightMap.put(c, value + 1);
                } else
                    highlightMap.put(c, 1);
            }
        }

        for (FoundWord foundWord : foundWords) {
            TableRow row = new TableRow(requireActivity().getApplicationContext());

            TextView word = buildWordTextForRow(foundWord);
            row.addView(word);

            TextView location = buildLocationTextForRow(foundWord);
            row.addView(location);

            CheckBox checkBox = new CheckBox(requireActivity().getApplicationContext());
            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, 1f);
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
            checkBox.setLayoutParams(params);
            checkBox.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            checkBox.setChecked(true);
            checkBox.setOnCheckedChangeListener((arg0, checked) -> {
                List<Coordinate> coordinates = wordsearchUtils.getCoordinatesBetweenPoints(foundWord.start, foundWord.end);
                if (!checked) {
                    for (Coordinate coordinate : coordinates) {
                        Integer value = highlightMap.get(coordinate);
                        if (value != null) {
                            if (value == 1) {
                                TableRow coordinateRow = (TableRow) solvedWordsearchGrid.getChildAt(coordinate.x);
                                TextView gridLetter = (TextView) coordinateRow.getChildAt(coordinate.y);
                                gridLetter.setBackgroundColor(Color.parseColor("#FAFAFA"));
                            }
                            highlightMap.put(coordinate, value - 1);
                        }
                    }
                } else {
                    for (Coordinate coordinate : coordinates) {
                        TableRow coordinateRow = (TableRow) solvedWordsearchGrid.getChildAt(coordinate.x);
                        TextView gridLetter = (TextView) coordinateRow.getChildAt(coordinate.y);
                        gridLetter.setBackgroundColor(foundWord.wordColour);
                        Integer value = highlightMap.get(coordinate);
                        if (value != null)
                            highlightMap.put(coordinate, value + 1);
                    }
                }
            });
            row.addView(checkBox);

            solvedWordsGrid.addView(row);
        }
    }

    private TableRow buildHeadingRow() {
        TableRow headingRow = new TableRow(requireActivity().getApplicationContext());
        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, 0, 0, 15);
        headingRow.setLayoutParams(layoutParams);

        TextView nameHeading = new TextView(requireActivity().getApplicationContext());
        nameHeading.setText(R.string.table_title_word);
        nameHeading.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, 1f));
        nameHeading.setSingleLine();
        nameHeading.setTextColor(Color.BLACK);
        nameHeading.setTextSize(17f);
        nameHeading.setTypeface(nameHeading.getTypeface(), Typeface.BOLD);
        nameHeading.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        headingRow.addView(nameHeading);

        TextView loadHeading = new TextView(requireActivity().getApplicationContext());
        loadHeading.setText(R.string.table_title_location);
        loadHeading.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, 1f));
        loadHeading.setSingleLine();
        loadHeading.setTextColor(Color.BLACK);
        loadHeading.setTextSize(17f);
        loadHeading.setTypeface(loadHeading.getTypeface(), Typeface.BOLD);
        loadHeading.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        headingRow.addView(loadHeading);

        TextView deleteHeading = new TextView(requireActivity().getApplicationContext());
        deleteHeading.setText(R.string.table_title_highlight);
        deleteHeading.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, 1f));
        deleteHeading.setSingleLine();
        deleteHeading.setTextColor(Color.BLACK);
        deleteHeading.setTextSize(17f);
        deleteHeading.setTypeface(deleteHeading.getTypeface(), Typeface.BOLD);
        deleteHeading.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        headingRow.addView(deleteHeading);

        return headingRow;
    }

    private TextView buildLocationTextForRow(FoundWord foundWord) {
        TextView location = new TextView(requireActivity().getApplicationContext());
        location.setText(String.format("%s to %s", foundWord.start.toString(), foundWord.end.toString()));
        location.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, 1f));
        location.setSingleLine();
        location.setTextColor(Color.BLACK);
        location.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        return location;
    }

    private TextView buildWordTextForRow(FoundWord foundWord) {
        TextView word = new TextView(requireActivity().getApplicationContext());
        word.setText(foundWord.word);
        word.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, 1f));
        word.setSingleLine();
        word.setTextColor(Color.BLACK);
        word.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        return word;
    }
}