package com.tjr.wordsearchsolver.data;

import androidx.annotation.NonNull;

public class FoundWord {

    public final Coordinate start;
    public final Coordinate end;
    public final String word;
    public int wordColour;

    public FoundWord(String word, Coordinate start, Coordinate end) {
        this.word = word;
        this.start = start;
        this.end = end;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("%s: %s to %s", word, start.toString(), end.toString());
    }

    @Override
    public boolean equals(Object foundWord) {
        if (foundWord.getClass().equals(FoundWord.class)) {
            FoundWord fWord = (FoundWord) foundWord;
            return this.word.equals(fWord.word) && this.start.equals(fWord.start) && this.end.equals(fWord.end);
        }
        return false;
    }
}
