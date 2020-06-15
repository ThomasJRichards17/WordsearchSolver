package com.tjr.wordsearchsolver.ui.solutions;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.tjr.wordsearchsolver.R;
import com.tjr.wordsearchsolver.data.Solution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class SolutionsFragment extends Fragment {

    List<Solution> savedSolutions;
    private Logger logger = LoggerFactory.getLogger(SolutionsFragment.class);
    private TableLayout savedSolutionsTable;
    private TableLayout loadedWordsearchGrid;
    private EditText solvedWordsText;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_solutions, container, false);

        savedSolutionsTable = root.findViewById(R.id.savedSolutionsGrid);
        loadedWordsearchGrid = root.findViewById(R.id.loadedWordsearchGrid);
        solvedWordsText = root.findViewById(R.id.solvedWordsText);

        return root;
    }

    private void drawSavedSolutionsTable() {
        for (Solution solution : savedSolutions) {
            TableRow row = new TableRow(requireActivity().getApplicationContext());
            TextView text = new TextView(requireActivity().getApplicationContext());
            text.setText(solution.name);
            text.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            text.setSingleLine();
            text.setTextColor(Color.BLACK);
            text.setGravity(Gravity.CENTER);
            row.addView(text);
        }
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


}