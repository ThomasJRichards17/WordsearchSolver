package com.tjr.wordsearchsolver.ui.solutions;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.tjr.wordsearchsolver.R;
import com.tjr.wordsearchsolver.data.Coordinate;
import com.tjr.wordsearchsolver.data.FoundWord;
import com.tjr.wordsearchsolver.data.Solution;
import com.tjr.wordsearchsolver.utils.WordsearchUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SolutionsFragment extends Fragment {

    private final Logger logger = LoggerFactory.getLogger(SolutionsFragment.class);

    private List<Solution> savedSolutions;
    private TableLayout savedSolutionsTable;
    private TextView loadedSolutionHeading;
    private TableLayout loadedWordsearchGrid;
    private EditText solvedWordsText;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_solutions, container, false);

        savedSolutionsTable = root.findViewById(R.id.savedSolutionsGrid);
        loadedSolutionHeading = root.findViewById(R.id.loaded_solution_heading);
        loadedWordsearchGrid = root.findViewById(R.id.loadedWordsearchGrid);
        solvedWordsText = root.findViewById(R.id.solvedWordsText);

        drawSavedSolutionsTable();

        return root;
    }

    private void drawSavedSolutionsTable() {
        loadSavedSolutions();

        savedSolutionsTable.removeAllViews();

        if (savedSolutions.size() > 0) {
            savedSolutionsTable.addView(buildHeadingRow());

            for (Solution solution : savedSolutions) {
                TableRow row = new TableRow(requireActivity().getApplicationContext());
                TextView text = new TextView(requireActivity().getApplicationContext());
                text.setText(solution.name);
                text.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, 1f));
                text.setSingleLine();
                text.setTextColor(Color.BLACK);
                text.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                row.addView(text);

                ImageButton loadButton = new ImageButton(requireActivity().getApplicationContext());
                loadButton.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                loadButton.setBackground(text.getBackground());
                loadButton.setImageResource(R.drawable.ic_load);
                loadButton.setOnClickListener(v -> {
                    loadedSolutionHeading.setVisibility(View.VISIBLE);
                    solvedWordsText.setVisibility(View.VISIBLE);
                    loadSavedSolution(solution.name);
                });
                row.addView(loadButton);

                ImageButton deleteButton = new ImageButton(requireActivity().getApplicationContext());
                deleteButton.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                deleteButton.setBackground(text.getBackground());
                deleteButton.setImageResource(R.drawable.ic_delete);
                deleteButton.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Are you sure you want to delete the saved solution?");
                    builder.setPositiveButton("Yes", (dialog, which) -> {
                        deleteSavedSolution(solution.name);
                        drawSavedSolutionsTable();
                    });
                    builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());
                    builder.show();
                });
                row.addView(deleteButton);

                savedSolutionsTable.addView(row);
            }
        } else {
            TableRow row = new TableRow(requireActivity().getApplicationContext());
            TextView text = new TextView(requireActivity().getApplicationContext());
            text.setText(R.string.no_solutions_saved);
            text.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1f));
            text.setTextColor(Color.BLACK);
            text.setGravity(Gravity.CENTER_HORIZONTAL);
            row.addView(text);
            savedSolutionsTable.addView(row);
        }
    }

    private TableRow buildHeadingRow() {
        TableRow headingRow = new TableRow(requireActivity().getApplicationContext());
        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, 0, 0, 15);
        headingRow.setLayoutParams(layoutParams);

        TextView nameHeading = new TextView(requireActivity().getApplicationContext());
        nameHeading.setText(R.string.table_title_solution);
        nameHeading.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, 1f));
        nameHeading.setSingleLine();
        nameHeading.setTextColor(Color.BLACK);
        nameHeading.setTextSize(17f);
        nameHeading.setTypeface(nameHeading.getTypeface(), Typeface.BOLD);
        nameHeading.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        headingRow.addView(nameHeading);

        TextView loadHeading = new TextView(requireActivity().getApplicationContext());
        loadHeading.setText(R.string.table_title_load);
        loadHeading.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, 1f));
        loadHeading.setSingleLine();
        loadHeading.setTextColor(Color.BLACK);
        loadHeading.setTextSize(17f);
        loadHeading.setTypeface(loadHeading.getTypeface(), Typeface.BOLD);
        loadHeading.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        headingRow.addView(loadHeading);

        TextView deleteHeading = new TextView(requireActivity().getApplicationContext());
        deleteHeading.setText(R.string.table_title_delete);
        deleteHeading.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, 1f));
        deleteHeading.setSingleLine();
        deleteHeading.setTextColor(Color.BLACK);
        deleteHeading.setTextSize(17f);
        deleteHeading.setTypeface(deleteHeading.getTypeface(), Typeface.BOLD);
        deleteHeading.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        headingRow.addView(deleteHeading);

        return headingRow;
    }

    private void loadSavedSolutions() {
        Gson gson = new Gson();
        savedSolutions = new ArrayList<>();
        String fileName = "saved_solutions.json";

        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File solutionsFile = new File(storageDir, fileName);
        if (solutionsFile.exists()) {
            try {
                savedSolutions = gson.fromJson(new JsonReader(new FileReader(solutionsFile)), new TypeToken<ArrayList<Solution>>() {
                }.getType());
            } catch (FileNotFoundException e) {
                logger.error("Error loading saved solutions - ", e);
            }
        }
    }

    private void loadSavedSolution(String solutionName) {
        Solution solution = getSolutionFromList(solutionName);
        if (solution != null) {
            updateFoundWordsText(solution);
            drawSolvedWordsearchGrid(solution);
            highlightFoundWords(solution.foundWords);
        }
    }

    private void drawSolvedWordsearchGrid(Solution solution) {
        loadedWordsearchGrid.removeAllViews();
        for (int i = 0; i < solution.grid.size(); i++)
            loadedWordsearchGrid.addView(buildEmptyRow(solution.grid.get(i)));
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
        WordsearchUtils wordsearchUtils = new WordsearchUtils();
        HashMap<Coordinate, Integer> colourMapping = new HashMap<>();
        List<List<Coordinate>> coordinatesLists = new ArrayList<>();

        for (FoundWord foundWord : foundWords) {
            List<Coordinate> coordinates = wordsearchUtils.getCoordinatesBetweenPoints(foundWord.start, foundWord.end);
            for (Coordinate coordinate : coordinates)
                colourMapping.put(coordinate, foundWord.wordColour);
            coordinatesLists.add(coordinates);
        }

        for (List<Coordinate> coordinates : coordinatesLists) {
            for (Coordinate coordinate : coordinates) {
                Integer colour = colourMapping.get(coordinate);
                TableRow row = (TableRow) loadedWordsearchGrid.getChildAt(coordinate.x);
                TextView text = (TextView) row.getChildAt(coordinate.y);
                if (colour != null)
                    text.setBackgroundColor(colour);
            }
        }
    }

    private void deleteSavedSolution(String solutionName) {
        savedSolutions.remove(getSolutionFromList(solutionName));

        Gson gson = new Gson();
        String fileName = "saved_solutions.json";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File solutionsFile = new File(storageDir, fileName);

        try (FileWriter writer = new FileWriter(solutionsFile)) {
            gson.toJson(savedSolutions, writer);
            writer.flush();
        } catch (IOException e) {
            logger.error("Error deleting saved solution - ", e);
            return;
        }

        Snackbar solutionDeleted = Snackbar.make(requireActivity().findViewById(R.id.navigation_solutions), "Solution deleted!", Snackbar.LENGTH_SHORT);
        solutionDeleted.setBackgroundTint(Color.parseColor("#228B22"));
        solutionDeleted.show();
    }

    private void updateFoundWordsText(Solution solution) {
        StringBuilder builder = new StringBuilder();
        for (FoundWord foundWord : solution.foundWords)
            builder.append(foundWord.toString()).append("\n");
        solvedWordsText.setText(builder.toString().trim());
    }

    private Solution getSolutionFromList(String solutionName) {
        for (Solution solution : savedSolutions) {
            if (solution.name.equals(solutionName))
                return solution;
        }
        return null;
    }

}