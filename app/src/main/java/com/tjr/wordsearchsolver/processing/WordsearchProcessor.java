package com.tjr.wordsearchsolver.processing;

import com.tjr.wordsearchsolver.data.Trie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordsearchProcessor {
    private Set<String> result = new HashSet<>();

    public List<String> findWords(char[][] board, String[] words) {

        Trie trie = new Trie();
        for (String word : words) {
            trie.insert(word);
        }

        List<Integer> xs = new ArrayList<>();
        List<Integer> ys = new ArrayList<>();

        int m = board.length;
        int n = board[0].length;

        boolean[][] visited = new boolean[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++)
                dfs(board, visited, "", i, j, new ArrayList<>(), new ArrayList<>(), trie);
        }

        return new ArrayList<>(result);
    }

    private void dfs(char[][] board, boolean[][] visited, String str, int i, int j, List<Integer> xs, List<Integer> ys, Trie trie) {
        int m = board.length;
        int n = board[0].length;

        xs.add(i);
        ys.add(j);

        if (i < 0 || j < 0 || i >= m || j >= n) {
            xs.remove(xs.size() - 1);
            ys.remove(ys.size() - 1);
            return;
        }

        if (visited[i][j]) {
            xs.remove(xs.size() - 1);
            ys.remove(ys.size() - 1);
            return;
        }

        str = str + board[i][j];

        if (!trie.startsWith(str)) {
            xs.remove(xs.size() - 1);
            ys.remove(ys.size() - 1);
            return;
        }

        if (trie.search(str)) {
            result.add(str);

            for (char ignored : str.toCharArray()) {
                xs.remove(xs.size() - 1);
                ys.remove(ys.size() - 1);
            }
        }

        visited[i][j] = true;
        dfs(board, visited, str, i - 1, j, xs, ys, trie);
        dfs(board, visited, str, i + 1, j, xs, ys, trie);
        dfs(board, visited, str, i, j - 1, xs, ys, trie);
        dfs(board, visited, str, i, j + 1, xs, ys, trie);
        visited[i][j] = false;
    }
}
