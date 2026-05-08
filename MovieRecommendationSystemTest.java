package Movie_Recommendation_System_Project;

import Movie_Recommendation_System_Project.engine.RecommendationEngine;
import Movie_Recommendation_System_Project.model.Movie;
import Movie_Recommendation_System_Project.model.User;
import Movie_Recommendation_System_Project.output.OutputFormatter;
import Movie_Recommendation_System_Project.parser.FileParser;
import Movie_Recommendation_System_Project.validation.Validator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * JUnit 4 test suite for the Movie Recommendation System.
 * Each test writes its own movies.txt / users.txt, runs the system logic,
 * and asserts the content of output.txt.
 *
 * Tests are fully independent: temporary files are created in @Before and
 * deleted in @After.
 */
public class MovieRecommendationSystemTest {

    // Paths used by every test (same names as the real application)
    private static final String MOVIES_FILE = "movies.txt";
    private static final String USERS_FILE  = "users.txt";
    private static final String OUTPUT_FILE = "output.txt";

    // -----------------------------------------------------------------------
    // Setup / Teardown
    // -----------------------------------------------------------------------

    @BeforeEach
    public void setUp() throws Exception {
        // Ensure no leftover files from a previous run
        deleteIfExists(MOVIES_FILE);
        deleteIfExists(USERS_FILE);
        deleteIfExists(OUTPUT_FILE);
    }

    @AfterEach
    public void tearDown() throws Exception {
//        deleteIfExists(MOVIES_FILE);
//        deleteIfExists(USERS_FILE);
//        deleteIfExists(OUTPUT_FILE);
    }

    // -----------------------------------------------------------------------
    // Helper utilities
    // -----------------------------------------------------------------------

    /** Writes the given content to the specified file. */
    private void writeFile(String filename, String content) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.print(content);
        }
    }

    /**
     * Runs the full system pipeline (parse → validate → recommend → format)
     * and writes the result to output.txt, mirroring what Main.java does.
     */
    private void runSystem() throws IOException {
        FileParser parser = new FileParser();
        List<Movie> movies = parser.parseMovies(MOVIES_FILE);
        List<User>  users  = parser.parseUsers(USERS_FILE);

        Validator validator = new Validator();
        String error = validator.validate(movies, users);

        String result;
        if (error != null) {
            result = error;
        } else {
            RecommendationEngine engine = new RecommendationEngine();
            Map<User, Map<String, List<Movie>>> recommendations = engine.recommend(users, movies);
            OutputFormatter formatter = new OutputFormatter();
            result = formatter.formatRecommendations(recommendations);
        }

        writeFile(OUTPUT_FILE, result);
    }

    /** Reads the entire output.txt and returns it as a trimmed String. */
    private String readOutput() throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(OUTPUT_FILE));
        return new String(bytes).trim();
    }

    /** Deletes a file if it exists; silently does nothing otherwise. */
    private void deleteIfExists(String filename) {
        new File(filename).delete();
    }

    // -----------------------------------------------------------------------
    // TC01 – Valid movie data accepted
    // -----------------------------------------------------------------------
    @Test
    public void testTC01_ValidMovieData() throws Exception {
        // A correctly formatted movie should be accepted without any error
        writeFile(MOVIES_FILE, "The Nun,TN123\nhorror,drama\n");
        writeFile(USERS_FILE,  "Ahmed Ali,12345678A\nhorror\n");

        runSystem();
        String output = readOutput();

        assertFalse(output.contains("ERROR"),
                "TC01: No error expected for valid movie data");
    }

    // -----------------------------------------------------------------------
    // TC02 – Valid user data accepted
    // -----------------------------------------------------------------------
    @Test
    public void testTC02_ValidUserData() throws Exception {
        // A correctly formatted user should be accepted without any error
        writeFile(MOVIES_FILE, "The Nun,TN123\nhorror\n");
        writeFile(USERS_FILE,  "Ahmed Ali,12345678A\nhorror,action\n");

        runSystem();
        String output = readOutput();

        assertFalse(output.contains("ERROR"),
                "TC02: No error expected for valid user data");
    }

    // -----------------------------------------------------------------------
    // TC03 – Invalid movie title (word starts with lowercase)
    // -----------------------------------------------------------------------
    @Test
    public void testTC03_InvalidMovieTitle() throws Exception {
        // 'the' is not capitalized → title error expected
        writeFile(MOVIES_FILE, "the Nun,TN123\nhorror\n");
        writeFile(USERS_FILE,  "Ahmed Ali,12345678A\nhorror\n");

        runSystem();
        String output = readOutput();

        assertEquals("Movie Title ERROR: the Nun is wrong", output,
                "TC03: Expected movie title error");
    }

    // -----------------------------------------------------------------------
    // TC04 – Wrong movie ID letters (initials don't match title)
    // -----------------------------------------------------------------------
    @Test
    public void testTC04_WrongMovieIdLetters() throws Exception {
        // Title initials are 'TN' but ID has 'ABC' → letters error
        writeFile(MOVIES_FILE, "The Nun,ABC123\nhorror\n");
        writeFile(USERS_FILE,  "Ahmed Ali,12345678A\nhorror\n");

        runSystem();
        String output = readOutput();

        assertEquals("Movie Id letters ERROR: ABC123 are wrong", output,
                "TC04: Expected movie ID letters error");
    }

    // -----------------------------------------------------------------------
    // TC05 – Non-unique movie ID digits
    // -----------------------------------------------------------------------
    @Test
    public void testTC05_NonUniqueMovieIdDigits() throws Exception {
        // Digits '111' are not unique → numbers error
        writeFile(MOVIES_FILE, "The Nun,TN111\nhorror\n");
        writeFile(USERS_FILE,  "Ahmed Ali,12345678A\nhorror\n");

        runSystem();
        String output = readOutput();

        assertEquals("Movie Id numbers ERROR: TN111 aren't unique", output,
                "TC05: Expected movie ID numbers error");
    }

    // -----------------------------------------------------------------------
    // TC06 – Username contains a digit
    // -----------------------------------------------------------------------
    @Test
    public void testTC06_UsernameContainsDigit() throws Exception {
        // 'Ahmed2' contains a digit → username error
        writeFile(MOVIES_FILE, "The Nun,TN123\nhorror\n");
        writeFile(USERS_FILE,  "Ahmed2,12345678A\naction\n");

        runSystem();
        String output = readOutput();

        assertEquals("Username ERROR: Ahmed2 is wrong", output,
                "TC06: Expected username error");
    }

    // -----------------------------------------------------------------------
    // TC07 – Username starts with a space
    // -----------------------------------------------------------------------
    @Test
    public void testTC07_UsernameStartsWithSpace() throws Exception {
        // Leading space in username is invalid
        writeFile(MOVIES_FILE, "The Nun,TN123\nhorror\n");
        writeFile(USERS_FILE,  " Ahmed,12345678A\naction\n");

        runSystem();
        String output = readOutput();

        assertEquals("Username ERROR:  Ahmed is wrong", output,
                "TC07: Expected username error for leading space");
    }

    // -----------------------------------------------------------------------
    // TC08 – User ID too short
    // -----------------------------------------------------------------------
    @Test
    public void testTC08_UserIdTooShort() throws Exception {
        // '12345A' is only 6 characters → user ID error
        writeFile(MOVIES_FILE, "The Nun,TN123\nhorror\n");
        writeFile(USERS_FILE,  "Ahmed Ali,12345A\nhorror\n");

        runSystem();
        String output = readOutput();

        assertEquals("User Id ERROR: 12345A is wrong", output,
                "TC08: Expected user ID error for short ID");
    }

    // -----------------------------------------------------------------------
    // TC09 – User ID starts with a letter
    // -----------------------------------------------------------------------
    @Test
    public void testTC09_UserIdStartsWithLetter() throws Exception {
        // 'A23456789' starts with a letter → user ID error
        writeFile(MOVIES_FILE, "The Nun,TN123\nhorror\n");
        writeFile(USERS_FILE,  "Ahmed Ali,A23456789\nhorror\n");

        runSystem();
        String output = readOutput();

        assertEquals("User Id ERROR: A23456789 is wrong", output,
                "TC09: Expected user ID error for letter-first ID");
    }

    // -----------------------------------------------------------------------
    // TC10 – User ID ends with two letters
    // -----------------------------------------------------------------------
    @Test
    public void testTC10_UserIdEndsWithTwoLetters() throws Exception {
        // '1234567AB' ends with two letters → user ID error
        writeFile(MOVIES_FILE, "The Nun,TN123\nhorror\n");
        writeFile(USERS_FILE,  "Ahmed Ali,1234567AB\nhorror\n");

        runSystem();
        String output = readOutput();

        assertEquals("User Id ERROR: 1234567AB is wrong", output,
                "TC10: Expected user ID error for two trailing letters");
    }

    // -----------------------------------------------------------------------
    // TC11 – Recommendation for one liked category
    // -----------------------------------------------------------------------
    @Test
    public void testTC11_RecommendationOneLikedCategory() throws Exception {
        // User likes horror; only The Nun matches → horror recommendation
        writeFile(MOVIES_FILE,
                "The Nun,TN123\nhorror\n" +
                "John Wick,JW456\naction\n");
        writeFile(USERS_FILE,
                "Ahmed Ali,12345678A\nhorror\n");

        runSystem();
        String output = readOutput();

        assertTrue(output.contains("For User: Ahmed Ali"),
                "TC11: Output must contain 'For User: Ahmed Ali'");
        assertTrue(output.contains("TN123-The Nun"),
                "TC11: Output must contain horror recommendation");
    }

    // -----------------------------------------------------------------------
    // TC12 – Recommendations for multiple liked categories
    // -----------------------------------------------------------------------
    @Test
    public void testTC12_RecommendationsMultipleCategories() throws Exception {
        // Sara likes horror and drama; should get both
        writeFile(MOVIES_FILE,
                "The Nun,TN123\nhorror\n" +
                "John Wick,JW456\naction\n" +
                "Great Drama,GD789\ndrama\n");
        writeFile(USERS_FILE,
                "Sara Ali,123456789\nhorror,drama\n");

        runSystem();
        String output = readOutput();

        assertTrue(output.contains("For User: Sara Ali"),
                "TC12: Output must contain 'For User: Sara Ali'");
        assertTrue(output.contains("TN123-The Nun"),
                "TC12: Output must contain horror recommendation");
        assertTrue(output.contains("GD789-Great Drama"),
                "TC12: Output must contain drama recommendation");
    }

    // -----------------------------------------------------------------------
    // TC13 – All movies in liked category are recommended
    // -----------------------------------------------------------------------
    @Test
    public void testTC13_AllMoviesInCategoryRecommended() throws Exception {
        // Two horror movies; user likes horror → both must appear (order may vary)
        writeFile(MOVIES_FILE,
                "The Nun,TN123\nhorror\n" +
                "Annabelle,A456\nhorror\n");
        writeFile(USERS_FILE,
                "Mona Adel,12345678B\nhorror\n");

        runSystem();
        String output = readOutput();

        assertTrue(output.contains("TN123-The Nun"),
                "TC13: TN123-The Nun must be in output");
        assertTrue(output.contains("A456-Annabelle"),
                "TC13: A456-Annabelle must be in output");
    }

    // -----------------------------------------------------------------------
    // TC14 – Output format is correct
    // -----------------------------------------------------------------------
    @Test
    public void testTC14_OutputFormatIsCorrect() throws Exception {
        // Verify the exact format: "For User: name, id" and "category: id-title"
        writeFile(MOVIES_FILE, "The Nun,TN123\nhorror\n");
        writeFile(USERS_FILE,  "Ahmed Ali,12345678A\nhorror\n");

        runSystem();
        String output = readOutput();

        assertTrue(output.contains("For User:"),
                "TC14: Output must contain 'For User:' header");
        assertTrue(output.contains("TN123-The Nun"),
                "TC14: Output must contain movie id-title format");
    }

    // -----------------------------------------------------------------------
    // TC15 – System stops at first encountered error
    // -----------------------------------------------------------------------
    @Test
    public void testTC15_StopsAtFirstError() throws Exception {
        // First error: invalid title ('the Nun'). Second would be ID letters.
        // Only the title error should appear.
        writeFile(MOVIES_FILE,
                "the Nun,ABC123\nhorror\n");
        writeFile(USERS_FILE,
                "Ahmed Ali,12345678A\nhorror\n");

        runSystem();
        String output = readOutput();

        assertEquals("Movie Title ERROR: the Nun is wrong", output,
                "TC15: Only the first error (title) should appear");
        assertFalse(output.contains("Movie Id"),
                "TC15: Letter error must NOT appear in output");
    }

    // -----------------------------------------------------------------------
    // TC16 – Multiple users get separate recommendation sections
    // -----------------------------------------------------------------------
    @Test
    public void testTC16_MultipleUsersSeparateSections() throws Exception {
        // Two users with different preferred categories
        writeFile(MOVIES_FILE,
                "The Nun,TN123\nhorror\n" +
                "John Wick,JW456\naction\n");
        writeFile(USERS_FILE,
                "Ahmed Ali,12345678A\nhorror\n" +
                "Sara Ali,123456789\naction\n");

        runSystem();
        String output = readOutput();

        assertTrue(output.contains("For User: Ahmed Ali"),
                "TC16: First user section must be present");
        assertTrue(output.contains("For User: Sara Ali"),
                "TC16: Second user section must be present");
        assertTrue(output.contains("TN123-The Nun"),
                "TC16: Ahmed must get horror movie");
        assertTrue(output.contains("JW456-John Wick"),
                "TC16: Sara must get action movie");
    }

    // -----------------------------------------------------------------------
    // TC17 – User liked categories produce correct recommendations
    // -----------------------------------------------------------------------
    @Test
    public void testTC17_MultipleCategoriesToCorrectRecommendations() throws Exception {
        // User likes two categories; both recommendation lines must appear
        writeFile(MOVIES_FILE,
                "The Nun,TN123\nhorror\n" +
                "John Wick,JW456\naction\n");
        writeFile(USERS_FILE,
                "Ahmed Ali,12345678A\nhorror,action\n");

        runSystem();
        String output = readOutput();

        assertTrue(output.contains("TN123-The Nun"),
                "TC17: horror recommendation must be present");
        assertTrue(output.contains("JW456-John Wick"),
                "TC17: action recommendation must be present");
    }

    // -----------------------------------------------------------------------
    // TC18 – Valid movie categories are accepted
    // -----------------------------------------------------------------------
    @Test
    public void testTC18_ValidMovieCategoriesAccepted() throws Exception {
        // All three movies use allowed categories → no error expected
        writeFile(MOVIES_FILE,
                "The Nun,TN123\nhorror\n" +
                "John Wick,JWI456\naction\n" +
                "Great Drama,GD789\ndrama\n");
        writeFile(USERS_FILE,
                "Ahmed Ali,12345678A\nhorror\n");

        runSystem();
        String output = readOutput();

        assertFalse(output.contains("ERROR"),
                "TC18: No error expected for valid categories");
    }

    // -----------------------------------------------------------------------
    // TC19 – User with no liked categories
    // -----------------------------------------------------------------------
    @Test
    public void testTC19_UserWithNoLikedCategories() throws Exception {
        // The FileParser filters out blank lines, so a truly empty category line
        // cannot be represented in a text file. Instead we directly build the
        // model objects and run the engine + formatter to verify behaviour.
        List<Movie> movies = Arrays.asList(
                new Movie("The Nun", "TN123", Arrays.asList("horror"))
        );
        // User has an empty liked-categories list
        List<User> users = Arrays.asList(
                new User("Omar Ali", "12345678C", Collections.emptyList())
        );

        RecommendationEngine engine = new RecommendationEngine();
        Map<User, Map<String, List<Movie>>> recommendations = engine.recommend(users, movies);
        OutputFormatter formatter = new OutputFormatter();
        String output = formatter.formatRecommendations(recommendations).trim();

        writeFile(OUTPUT_FILE, output);   // persist for consistency

        assertTrue(output.contains("For User: Omar Ali"),
                "TC19: Output must contain the user header");
        assertFalse(output.contains("horror"),
                "TC19: Output must not contain horror recommendation");
    }

    // -----------------------------------------------------------------------
    // TC20 – Liked category with no matching movies
    // -----------------------------------------------------------------------
    @Test
    public void testTC20_LikedCategoryNoMatchingMovies() throws Exception {
        // User likes 'action' but no action movies exist → no movies listed under action
        writeFile(MOVIES_FILE, "The Nun,TN123\nhorror\n");
        writeFile(USERS_FILE,  "Mona Ali,12345678D\naction\n");

        runSystem();
        String output = readOutput();

        assertTrue(output.contains("For User: Mona Ali"),
                "TC20: Output must contain the user header");
        assertFalse(output.contains("TN123-The Nun"),
                "TC20: No horror movies should be recommended");
    }
}
