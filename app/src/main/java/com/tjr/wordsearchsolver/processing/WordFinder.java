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
            baseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
            String wordsFromImage = baseAPI.getUTF8Text();
            return formatWords(wordsFromImage);
        });
    }

    public Future<List<List<Character>>> recogniseWordsearch(Bitmap bitmap) {
        return executor.submit(() -> {
            baseAPI.setImage(bitmap);
            baseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
            String charsFromImage = baseAPI.getUTF8Text();
            return formatWordsearch(charsFromImage);
        });
    }

    private List<String> formatWords(String wordsFromImage) {
        ArrayList<String> words = new ArrayList<>();
        String[] unalteredWords = wordsFromImage.split("[ \n]");

        for (String word : unalteredWords) {
            String cleanedWord = word.replaceAll("\\P{L}", "").trim();
            if (!cleanedWord.isEmpty())
                words.add(cleanedWord.toLowerCase());
        }
        return words;
    }

    private List<List<Character>> formatWordsearch(String charsFromImage) {
        List<List<Character>> wordsearch = new ArrayList<>();
        return wordsearch;
    }

}
