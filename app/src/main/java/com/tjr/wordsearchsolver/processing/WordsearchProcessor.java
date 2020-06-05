package com.tjr.wordsearchsolver.processing;

import com.tjr.wordsearchsolver.data.Coordinate;
import com.tjr.wordsearchsolver.data.FoundWord;
import com.tjr.wordsearchsolver.data.SearchDirection;
import com.tjr.wordsearchsolver.data.SearchResponse;
import com.tjr.wordsearchsolver.data.Trie;

import java.util.ArrayList;
import java.util.List;

public class WordsearchProcessor {

    List<FoundWord> result = new ArrayList<>();

    public List<FoundWord> findWords(char[][] board, List<String> words) {

        int m = board.length;
        int n = board[0].length;

        Trie trie = buildTrie(words);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++)
                dfs(board, new boolean[m][n], "", i, j, new ArrayList<>(), trie, SearchDirection.HORIZONTAL);
        }

        removeFoundWords(words, result);
        trie = buildTrie(words);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++)
                dfs(board, new boolean[m][n], "", i, j, new ArrayList<>(), trie, SearchDirection.VERTICAL);
        }

        return result;
    }

    private void dfs(char[][] board, boolean[][] visited, String str, int i, int j, List<Coordinate> coordinates, Trie trie, SearchDirection direction) {
        int m = board.length;
        int n = board[0].length;

        coordinates.add(new Coordinate(i, j));

        if (i < 0 || j < 0 || i >= m || j >= n) {
            coordinates.remove(coordinates.size() - 1);
            return;
        }

        if (visited[i][j]) {
            coordinates.remove(coordinates.size() - 1);
            return;
        }

        str = str + board[i][j];

        if (!trie.startsWith(str)) {
            coordinates.remove(coordinates.size() - 1);
            return;
        }

        SearchResponse searchResponse = trie.search(str);
        if (searchResponse == SearchResponse.FOUND)
            result.add(new FoundWord(str, coordinates.get(coordinates.size() - str.length()), coordinates.get(coordinates.size() - 1)));
        else if (searchResponse.equals(SearchResponse.INCORRECT))
            coordinates.remove(coordinates.size() - 1);

        visited[i][j] = true;
        if (direction.equals(SearchDirection.VERTICAL)) {
            dfs(board, visited, str, i - 1, j, coordinates, trie, direction);
            dfs(board, visited, str, i + 1, j, coordinates, trie, direction);
        } else if (direction.equals(SearchDirection.HORIZONTAL)) {
            dfs(board, visited, str, i, j - 1, coordinates, trie, direction);
            dfs(board, visited, str, i, j + 1, coordinates, trie, direction);
        }
        visited[i][j] = false;

        if (coordinates.size() > 0)
            coordinates.remove(coordinates.size() - 1);
    }

    private Trie buildTrie(List<String> words) {
        Trie trie = new Trie();
        for (String word : words) {
            trie.insert(word);
        }
        return trie;
    }

    private void removeFoundWords(List<String> words, List<FoundWord> foundWords) {
        for (FoundWord foundWord : foundWords)
            words.remove(foundWord.word);
    }
}
