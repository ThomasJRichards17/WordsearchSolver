package com.tjr.wordsearchsolver.data;

import java.util.List;

public class Solution {

    public String name;
    public List<FoundWord> foundWords;
    public List<List<Character>> grid;

    public Solution(String name, List<FoundWord> foundWords, List<List<Character>> grid) {
        this.name = name;
        this.foundWords = foundWords;
        this.grid = grid;
    }
}
