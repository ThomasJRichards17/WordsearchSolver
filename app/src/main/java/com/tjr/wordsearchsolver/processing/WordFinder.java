package com.tjr.wordsearchsolver.processing;

import android.graphics.Bitmap;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WordFinder {

    private ExecutorService executor;

    public WordFinder() {
        executor = Executors.newSingleThreadExecutor();
    }

    public Future<List<String>> recogniseWords(Bitmap bitmap) {
        return executor.submit(() -> {
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
            Task<FirebaseVisionText> result = detector.processImage(image);
            try {
                FirebaseVisionText authResult = Tasks.await(result);
                return formatWords(authResult.getText());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
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
