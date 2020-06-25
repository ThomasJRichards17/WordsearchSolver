package com.tjr.wordsearchsolver.data;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

public class Coordinate {

    public final int x;
    public final int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public String toString() {
        return String.format("(%d, %d)", y + 1, x + 1);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + x;
        hash = 31 * hash + y;
        return hash;
    }

    public boolean equals(Object coordinate) {
        if (coordinate.getClass().equals(Coordinate.class)) {
            Coordinate coord = (Coordinate) coordinate;
            return this.x == coord.x && this.y == coord.y;
        }
        return false;
    }
}
