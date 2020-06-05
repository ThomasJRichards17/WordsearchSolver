package com.tjr.wordsearchsolver.data;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private static DataStore dataStore;
    private Uri wordsearchImagePath;
    private Uri searchWordsImagePath;
    private List<List<Character>> wordsearchGrid = new ArrayList<>();
    private List<String> searchWords = new ArrayList<>();
    private boolean isWordsearchFromCamera;
    private boolean areWordsFromCamera;
    
    /**
     * The private constructor for the Data Store Singleton class
     */
    private DataStore() {
    }

    public static DataStore getDataStore() {
        // Instantiate a new DataStore if we don't have one yet
        if (dataStore == null)
            dataStore = new DataStore();
        return dataStore;
    }

    public Uri getWordsearchImagePath() {
        return wordsearchImagePath;
    }

    public void setWordsearchImagePath(Uri wordsearchImagePath) {
        this.wordsearchImagePath = wordsearchImagePath;
    }

    public Uri getSearchWordsImagePath() {
        return searchWordsImagePath;
    }

    public void setSearchWordsImagePath(Uri searchWordsImagePath) {
        this.searchWordsImagePath = searchWordsImagePath;
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

    public boolean getIsWordsearchFromCamera() {
        return isWordsearchFromCamera;
    }

    public void setWordsearchFromCamera(boolean wordsearchFromCamera) {
        isWordsearchFromCamera = wordsearchFromCamera;
    }

    public boolean getAreWordsFromCamera() {
        return areWordsFromCamera;
    }

    public void setAreWordsFromCamera(boolean areWordsFromCamera) {
        this.areWordsFromCamera = areWordsFromCamera;
    }
}
