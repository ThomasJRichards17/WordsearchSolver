package com.tjr.wordsearchsolver.data;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private static DataStore dataStore;
    private Uri wordsearchImage;
    private Uri searchWordsImage;
    private List<List<Character>> wordsearchGrid = new ArrayList<>();
    private List<String> searchWords = new ArrayList<>();

    /**
     * The private constructor for the Data Store Singleton class
     */
    private DataStore() {
    }

    public static DataStore getDataStore() {
        // instantiate a new DataStore if we don't have one yet
        if (dataStore == null)
            dataStore = new DataStore();
        return dataStore;
    }

    public Uri getWordsearchImage() {
        return wordsearchImage;
    }

    public void setWordsearchImage(Uri wordsearchImage) {
        this.wordsearchImage = wordsearchImage;
    }

    public Uri getSearchWordsImage() {
        return searchWordsImage;
    }

    public void setSearchWordsImage(Uri searchWordsImage) {
        this.searchWordsImage = searchWordsImage;
    }

    public List<List<Character>> getWordsearchGrid() {
        return wordsearchGrid;
    }

    public void setWordsearchGrid(List<List<Character>> wordsearchGrid) {
        this.wordsearchGrid = wordsearchGrid;
    }

    public List<String> getSearchWords() {
        return searchWords;
    }

    public void setSearchWords(List<String> searchWords) {
        this.searchWords = searchWords;
    }
}
