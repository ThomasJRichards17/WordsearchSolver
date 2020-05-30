package com.tjr.wordsearchsolver.processing;

import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.tjr.wordsearchsolver.data.DataStore;

import java.util.ArrayList;
import java.util.List;

public class WordFinder {

    private final TessBaseAPI baseAPI;

    public WordFinder() {
        baseAPI = new TessBaseAPI();
        baseAPI.init(DataStore.getDataStore().getTrainedDataPath(), "eng");
    }

    public List<String> recogniseWords(Bitmap bitmap) {
        baseAPI.setImage(bitmap);
        String wordsFromImage = baseAPI.getUTF8Text();
        return formatWords(wordsFromImage);
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
