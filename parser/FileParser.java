package com.movierecommend.parser;

import com.movierecommend.model.Movie;
import com.movierecommend.model.User;

import java.io.*;
import java.util.*;

/**
 * Reads movies.txt and users.txt and returns raw (unvalidated) model objects.
 * Each file uses a repeating two-line block:
 *   Line 1: name , id
 *   Line 2: category1 , category2 , ...   (or liked_category1 , liked_category2 , ...)
 */
public class FileParser {

    /**
     * Parses movies.txt.
     * @param filePath path to the movies file
     * @return ordered list of Movie objects (may contain invalid data — validation is separate)
     * @throws IOException if the file cannot be read
     */
    public List<Movie> parseMovies(String filePath) throws IOException {
        List<Movie> movies = new ArrayList<>();
        List<String> lines = readNonEmptyLines(filePath);

        if (lines.size() % 2 != 0) {
            throw new IOException("movies.txt has an odd number of lines; each movie requires exactly 2 lines.");
        }

        for (int i = 0; i < lines.size(); i += 2) {
            String[] firstParts = splitOnComma(lines.get(i));
            // Use stripTrailing (not trim) so leading spaces are visible to the validator
            String title    = firstParts[0].stripTrailing();
            String movieId  = firstParts.length > 1 ? firstParts[1].trim() : "";

            String[] catParts = splitOnComma(lines.get(i + 1));
            List<String> categories = new ArrayList<>();
            for (String c : catParts) {
                categories.add(c.trim().toLowerCase());
            }

            movies.add(new Movie(title, movieId, categories));
        }
        return movies;
    }

    /**
     * Parses users.txt.
     * @param filePath path to the users file
     * @return ordered list of User objects (may contain invalid data — validation is separate)
     * @throws IOException if the file cannot be read
     */
    public List<User> parseUsers(String filePath) throws IOException {
        List<User> users = new ArrayList<>();
        List<String> lines = readNonEmptyLines(filePath);

        if (lines.size() % 2 != 0) {
            throw new IOException("users.txt has an odd number of lines; each user requires exactly 2 lines.");
        }

        for (int i = 0; i < lines.size(); i += 2) {
            String[] firstParts = splitOnComma(lines.get(i));
            // Use stripTrailing (not trim) so leading spaces are visible to the validator
            String username = firstParts[0].stripTrailing();
            String userId   = firstParts.length > 1 ? firstParts[1].trim() : "";

            String[] catParts = splitOnComma(lines.get(i + 1));
            List<String> liked = new ArrayList<>();
            for (String c : catParts) {
                liked.add(c.trim().toLowerCase());
            }

            users.add(new User(username, userId, liked));
        }
        return users;
    }

    // ------------------------------------------------------------------ helpers

    /** Reads a file and returns only non-blank lines, trimmed. */
    private List<String> readNonEmptyLines(String filePath) throws IOException {
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    result.add(line);
                }
            }
        }
        return result;
    }

    /** Splits a raw line on commas. Always returns at least one element. */
    private String[] splitOnComma(String line) {
        return line.split(",", -1);
    }
}
