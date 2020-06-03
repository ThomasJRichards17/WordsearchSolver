package com.tjr.wordsearchsolver.data;

public class Trie {
    private TrieNode root = new TrieNode();

    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (node.children[c - 'a'] == null)
                node.children[c - 'a'] = new TrieNode();
            node = node.children[c - 'a'];
        }
        node.item = word;
    }

    public boolean search(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (node.children[c - 'a'] == null)
                return false;
            node = node.children[c - 'a'];
        }
        return node.item.equals(word);
    }

    public boolean startsWith(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            if (node.children[c - 'a'] == null)
                return false;
            node = node.children[c - 'a'];
        }
        return true;
    }
}

class TrieNode {
    TrieNode[] children = new TrieNode[26];
    String item = "";
}