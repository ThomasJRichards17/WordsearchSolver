package com.tjr.wordsearchsolver.data;

import androidx.annotation.NonNull;

public class FoundWord {

    public String word;
    public Coordinate start;
    public Coordinate end;

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
}
