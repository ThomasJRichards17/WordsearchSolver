package com.tjr.wordsearchsolver.data;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

public class Coordinate {

    public int x;
    public int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public String toString() {
        return String.format("(%d, %d)", y, x);
    }
}
