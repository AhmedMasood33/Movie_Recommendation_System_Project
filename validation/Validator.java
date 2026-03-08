package Movie_Recommendation_System_Project.validation;

import Movie_Recommendation_System_Project.model.Movie;
import Movie_Recommendation_System_Project.model.User;

import java.util.*;

public class Validator {
        /** Allowed categories (lowercase). */
    private static final Set<String> ALLOWED_CATEGORIES = new LinkedHashSet<>(Arrays.asList(
            "horror", "action", "drama", "comedy", "thriller", "sci-fi", "romance"
    ));

    // ----------------------------------------------------------------------- public API

    /**
     * Validates the complete list of movies then users.
     * @return first error string, or null if all data is valid.
     */
    public String validate(List<Movie> movies, List<User> users) {
        // Validate movies
        for (Movie movie : movies) {
            String err = validateMovie(movie);
            if (err != null) return err;
        }
        // Validate users (tracking seen IDs for uniqueness)
        Set<String> seenUserIds = new HashSet<>();
        for (User user : users) {
            String err = validateUser(user, seenUserIds);
            if (err != null) return err;
            seenUserIds.add(user.getUserId());
        }
        return null; // all valid
    }

    // ----------------------------------------------------------------------- movie rules

    private String validateMovie(Movie movie) {
        // 1. Title: every word must start with a capital letter
        if (!isTitleValid(movie.getTitle())) {
            return "Movie Title ERROR: " + movie.getTitle() + " is wrong";
        }

        // 2. Movie ID letters: must equal the initials (first letter of each word) in UPPER CASE
        String expectedLetters = extractInitials(movie.getTitle());
        String movieId = movie.getMovieId();
        String idLetters = extractIdLetters(movieId);

        if (!idLetters.equals(expectedLetters)) {
            return "Movie Id letters ERROR: " + movieId + " are wrong";
        }

        // 3. Movie ID numbers: exactly 3 trailing digits, all unique
        String idDigits = extractIdDigits(movieId);
        if (idDigits.length() != 3 || !allUnique(idDigits)) {
            return "Movie Id numbers ERROR: " + movieId + " aren't unique";
        }

        // 4. Categories must all be in ALLOWED_CATEGORIES
        for (String cat : movie.getCategories()) {
            if (!ALLOWED_CATEGORIES.contains(cat)) {
                return "Movie Category ERROR: " + cat + " is wrong";
            }
        }

        return null;
    }

    // ----------------------------------------------------------------------- user rules

    private String validateUser(User user, Set<String> seenUserIds) {
        // 5. Username: alphabetic and spaces only; must NOT start with space
        if (!isUsernameValid(user.getUsername())) {
            return "Username ERROR: " + user.getUsername() + " is wrong";
        }

        // 6. User ID: exactly 9 alphanumeric chars, starts with digits, ends with at most 1 letter; unique
        String uid = user.getUserId();
        if (!isUserIdValid(uid) || seenUserIds.contains(uid)) {
            return "User Id ERROR: " + uid + " is wrong";
        }

        return null;
    }

    // ----------------------------------------------------------------------- helper methods

    /**
     * Every word (split by whitespace) must begin with an uppercase letter.
     * Words must be non-empty.
     */
    private boolean isTitleValid(String title) {
        if (title == null || title.isEmpty()) return false;
        String[] words = title.split("\\s+");
        if (words.length == 0) return false;
        for (String word : words) {
            if (word.isEmpty()) return false;
            if (!Character.isUpperCase(word.charAt(0))) return false;
        }
        return true;
    }

    /**
     * Extracts the first (uppercase) letter of each word in the title.
     * e.g. "Star Wars" -> "SW"
     */
    private String extractInitials(String title) {
        StringBuilder sb = new StringBuilder();
        for (String word : title.split("\\s+")) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        return sb.toString();
    }

    /**
     * Strips trailing digits from the ID and returns the remaining prefix (letters part).
     * e.g. "SW123" -> "SW"
     */
    private String extractIdLetters(String movieId) {
        int i = movieId.length() - 1;
        while (i >= 0 && Character.isDigit(movieId.charAt(i))) {
            i--;
        }
        return movieId.substring(0, i + 1);
    }

    /**
     * Returns only the trailing digit characters of the movie ID.
     * e.g. "SW123" -> "123"
     */
    private String extractIdDigits(String movieId) {
        int i = movieId.length() - 1;
        int start = movieId.length();
        while (i >= 0 && Character.isDigit(movieId.charAt(i))) {
            start = i;
            i--;
        }
        return movieId.substring(start);
    }

    /** Checks that all characters in a string are distinct. */
    private boolean allUnique(String s) {
        Set<Character> seen = new HashSet<>();
        for (char c : s.toCharArray()) {
            if (!seen.add(c)) return false;
        }
        return true;
    }

    /**
     * Username rules:
     *  - Contains only alphabetic characters and spaces
     *  - Must NOT start with a space
     */
    private boolean isUsernameValid(String username) {
        if (username == null || username.isEmpty()) return false;
        if (username.charAt(0) == ' ') return false;
        for (char c : username.toCharArray()) {
            if (!Character.isLetter(c) && c != ' ') return false;
        }
        return true;
    }

    /**
     * User ID rules:
     *  - Exactly 9 characters
     *  - All characters are alphanumeric
     *  - Starts with at least one digit
     *  - Ends with AT MOST one alphabetic character
     *    (i.e. the last character may be a letter; all preceding characters must be digits)
     */
    private boolean isUserIdValid(String uid) {
        if (uid == null || uid.length() != 9) return false;

        // All alphanumeric
        for (char c : uid.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) return false;
        }

        // Must start with a digit
        if (!Character.isDigit(uid.charAt(0))) return false;

        // At most one trailing letter:
        // Count letters from the end
        int trailingLetters = 0;
        for (int i = uid.length() - 1; i >= 0; i--) {
            if (Character.isLetter(uid.charAt(i))) trailingLetters++;
            else break;
        }
        return trailingLetters <= 1;
    }
}
