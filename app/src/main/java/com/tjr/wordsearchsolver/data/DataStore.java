package com.tjr.wordsearchsolver.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private static DataStore dataStore;
    private File wordsearchImage;
    private File searchWordsImage;
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

    public File getWordsearchImage() {
        return wordsearchImage;
    }

    public void setWordsearchImage(File wordsearchImage) {
        this.wordsearchImage = wordsearchImage;
    }

    public File getSearchWordsImage() {
        return searchWordsImage;
    }

    public void setSearchWordsImage(File searchWordsImage) {
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
