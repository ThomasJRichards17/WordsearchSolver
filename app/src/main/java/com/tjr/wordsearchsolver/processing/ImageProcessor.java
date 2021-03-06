package com.tjr.wordsearchsolver.processing;

import android.graphics.Bitmap;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ImageProcessor {

    private final Logger logger = LoggerFactory.getLogger(ImageProcessor.class);
    private final ExecutorService executor;

    public ImageProcessor() {
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
                logger.error("Error recognising words in image - ", e);
                return new ArrayList<>();
            }
        });
    }

    public Future<List<List<Character>>> recogniseWordsearch(Bitmap bitmap) {
        return executor.submit(() -> {
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
            Task<FirebaseVisionText> result = detector.processImage(image);
            try {
                FirebaseVisionText authResult = Tasks.await(result);
                return formatWordsearch(authResult.getText());
            } catch (ExecutionException | InterruptedException e) {
                return null;
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
        String[] unalteredLines = charsFromImage.split("\n");
        for (String line : unalteredLines) {
            line = line.replaceAll("\\P{L}", "").replaceAll(" ", "").toUpperCase().trim();
            char[] characters = line.toCharArray();
            if (characters.length > 1) {
                List<Character> chars = new ArrayList<>();
                for (Character c : characters)
                    chars.add(c);
                wordsearch.add(chars);
            }
        }
        return wordsearch;
    }

}
