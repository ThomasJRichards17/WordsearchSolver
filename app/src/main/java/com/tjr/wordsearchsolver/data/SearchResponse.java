package com.tjr.wordsearchsolver.data;

public enum SearchResponse {
    FOUND(0),
    INCOMPLETE(1),
    INCORRECT(2);

    public final int code;

    SearchResponse(int code) {
        this.code = code;
    }
}
