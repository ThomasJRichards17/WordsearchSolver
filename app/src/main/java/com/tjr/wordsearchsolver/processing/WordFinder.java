package com.tjr.wordsearchsolver.processing;

import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.tjr.wordsearchsolver.data.DataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WordFinder {

    private final TessBaseAPI baseAPI;

    private ExecutorService executor;

    public WordFinder() {
        baseAPI = new TessBaseAPI();
        baseAPI.init(DataStore.getDataStore().getTrainedDataPath(), "eng");
        executor = Executors.newSingleThreadExecutor();
    }

    public Future<List<String>> recogniseWords(Bitmap bitmap) {
        return executor.submit(() -> {
            baseAPI.setImage(bitmap);
            String wordsFromImage = baseAPI.getUTF8Text();
            return formatWords(wordsFromImage);
        });
    }

    private List<String> formatWords(String wordsFromImage) {
        ArrayList<String> words = new ArrayList<>();
        String[] unalteredWords = wordsFromImage.split(" ");

        for (String word : unalteredWords) {
            String cleanedWord = word.replaceAll("\\P{L}", "").trim();
            if (!cleanedWord.isEmpty())
                words.add(cleanedWord.toLowerCase());
        }
        return words;
    }

}
