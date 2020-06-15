package com.tjr.wordsearchsolver.data;

import androidx.annotation.NonNull;

public class FoundWord {

    public final Coordinate start;
    public final Coordinate end;
    private final String word;
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

    public boolean equals(FoundWord foundWord) {
        return this.word.equals(foundWord.word) && this.start.equals(foundWord.start) && this.end.equals(foundWord.end);
    }
}
