package com.tjr.wordsearchsolver.utils;

import android.graphics.Color;

import com.tjr.wordsearchsolver.data.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordsearchUtils {

    public List<Coordinate> getCoordinatesBetweenPoints(Coordinate start, Coordinate end) {
        List<Coordinate> coordinates = new ArrayList<>();

        int startX = start.x;
        int endX = end.x;
        int startY = start.y;
        int endY = end.y;

        if (startX > endX) {
            int diff = startX - endX;
            if (startY > endY) {
                for (int i = 0; i <= diff; i++)
                    coordinates.add(new Coordinate(startX - i, startY - i));
            } else if (endY > startY) {
                for (int i = 0; i <= diff; i++)
                    coordinates.add(new Coordinate(startX - i, startY + i));
            } else {
                for (int i = startX; i >= endX; i--)
                    coordinates.add(new Coordinate(i, startY));
            }
        } else if (startX < endX) {
            int diff = endX - startX;
            if (startY > endY) {
                for (int i = 0; i <= diff; i++)
                    coordinates.add(new Coordinate(startX + i, startY - i));
            } else if (endY > startY) {
                for (int i = 0; i <= diff; i++)
                    coordinates.add(new Coordinate(startX + i, startY + i));
            } else {
                for (int i = startX; i <= endX; i++)
                    coordinates.add(new Coordinate(i, startY));
            }
        } else {
            if (startY > endY) {
                for (int i = endY; i <= startY; i++)
                    coordinates.add(new Coordinate(startX, i));
            } else if (endY > startY) {
                for (int i = startY; i <= endY; i++)
                    coordinates.add(new Coordinate(startX, i));
            } else {
                coordinates.add(new Coordinate(startX, startY));
            }
        }
        return coordinates;
    }

    public int getRandomColour() {
        Random r = new Random();
        return Color.argb(255, r.nextInt(256), r.nextInt(256), r.nextInt(256));
    }

}
