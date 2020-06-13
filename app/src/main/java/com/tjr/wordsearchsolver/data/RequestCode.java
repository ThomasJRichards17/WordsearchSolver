package com.tjr.wordsearchsolver.data;

public enum RequestCode {
    LOAD_WORDSEARCH_PHOTO(1),
    TAKE_WORDSEARCH_PHOTO(2),
    LOAD_WORDS_PHOTO(3),
    TAKE_WORDS_PHOTO(4);

    public final int code;

    RequestCode(int code) {
        this.code = code;
    }
}

