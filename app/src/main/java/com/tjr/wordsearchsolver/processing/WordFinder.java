package com.tjr.wordsearchsolver.processing;

import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.tjr.wordsearchsolver.data.DataStore;

public class WordFinder {

    private final TessBaseAPI baseAPI;

    public WordFinder() {
        baseAPI = new TessBaseAPI();
        baseAPI.init(DataStore.getDataStore().getTrainedDataPath(), "eng");
    }

    public String recogniseWords(Bitmap bitmap) {
        baseAPI.setImage(bitmap);
        return baseAPI.getUTF8Text();
    }

}
