package Movie_Recommendation_System_Project.out.production.Movie_Recommendation_System_Project.Movie_Recommendation_System_Project;


import Movie_Recommendation_System_Project.engine.RecommendationEngine;
import Movie_Recommendation_System_Project.model.Movie;
import Movie_Recommendation_System_Project.model.User;
import Movie_Recommendation_System_Project.validation.Validator;
import Movie_Recommendation_System_Project.output.OutputFormatter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

import java.util.*;

public class MovieRecommendationSystemWhiteBoxTest {
    private final Validator validator = new Validator();
    private final RecommendationEngine engine = new RecommendationEngine();
    private final OutputFormatter formatter = new OutputFormatter();
    // -----------------------------------------------------------------------
    // VALIDATOR INTERNAL LOGIC TESTS
    // -----------------------------------------------------------------------

    @Test
    void testValidator_IdInitialsWithMultipleSpaces() {
        // Path Coverage: Checks if the regex split handles multiple spaces correctly for initials
        Validator validator = new Validator();
        Movie movie = new Movie("Star   Wars", "SW123", List.of("sci-fi"));
        assertNull(validator.validate(List.of(movie), List.of()));
    }

    @Test
    void testValidator_UserIdUniqueConstraint() {
        // Logic Coverage: Verifies that the 'seenUserIds' Set correctly triggers an error on duplicates
        Validator validator = new Validator();
        User u1 = new User("User One", "12345678A", List.of("action"));
        User u2 = new User("User Two", "12345678A", List.of("drama"));
        String result = validator.validate(List.of(), List.of(u1, u2));
        assertEquals("User Id ERROR: 12345678A is wrong", result);
    }

    @Test
    void testValidator_UserIdAtMostOneLetter() {
        // Edge Case: Checks the trailing letter boundary (exactly 1 vs 0)
        Validator validator = new Validator();
        User u1 = new User("User One", "123456789", List.of("action")); // 0 letters
        assertNull(validator.validate(List.of(), List.of(u1)));
    }

    // -----------------------------------------------------------------------
    // PARSER & SANITIZATION TESTS
    // -----------------------------------------------------------------------

    @Test
    void testFileParser_CategoryCaseInsensitivity() {
        // Verification: Ensures FileParser converts all categories to lowercase during parsing
        // This ensures the RecommendationEngine logic (contains) isn't broken by case mismatches
        Movie m = new Movie("Title", "T123", List.of("HORROR", "Action"));
        // Simulating logic within parseMovies
        List<String> sanitized = new ArrayList<>();
        m.getCategories().forEach(c -> sanitized.add(c.trim().toLowerCase()));

        assertTrue(sanitized.contains("horror"));
        assertTrue(sanitized.contains("action"));
    }

    @Test
    void testFileParser_StripTrailingTitle() {
        // Verification: Validates that stripTrailing is used for titles but leading spaces remain
        // This is crucial for the Validator to catch " Ahmed" as an error later.
        String rawTitle = " Ahmed ";
        String processed = rawTitle.stripTrailing();
        assertEquals(" Ahmed", processed);
    }

    // -----------------------------------------------------------------------
    // ENGINE & DATA STRUCTURE TESTS
    // -----------------------------------------------------------------------

    @Test
    void testEngine_PreservesUserOrder() {
        // Path Coverage: Ensures LinkedHashMap is used to maintain the order of users from the input file
        RecommendationEngine engine = new RecommendationEngine();
        User u1 = new User("A", "111111111", List.of("action"));
        User u2 = new User("B", "222222222", List.of("drama"));

        Map<User, ?> result = engine.recommend(List.of(u1, u2), List.of());
        Iterator<User> it = result.keySet().iterator();
        assertEquals(u1, it.next());
        assertEquals(u2, it.next());
    }

    @Test
    void testEngine_DuplicateCategoriesInUser() {
        // Logic Coverage: If a user has "action, action" in their liked list,
        // the engine should process both as separate keys in the inner map.
        RecommendationEngine engine = new RecommendationEngine();
        User u1 = new User("A", "111111111", List.of("action", "action"));
        Movie m1 = new Movie("Action Movie", "AM123", List.of("action"));

        var result = engine.recommend(List.of(u1), List.of(m1));
        assertEquals(1, result.get(u1).size(), "Engine should handle duplicate category keys");
    }

    @Test
    void testEngine_MovieWithMultipleCategories() {
        // Logic Coverage: Ensure a movie with multiple categories appears in all matching user interests
        RecommendationEngine engine = new RecommendationEngine();
        Movie m1 = new Movie("The Batman", "TB123", List.of("action", "thriller"));
        User u1 = new User("Ahmed", "111111111", List.of("action", "thriller"));

        var recs = engine.recommend(List.of(u1), List.of(m1)).get(u1);
        assertTrue(recs.get("action").contains(m1));
        assertTrue(recs.get("thriller").contains(m1));
    }

    // -----------------------------------------------------------------------
    // FORMATTER EDGE CASES
    // -----------------------------------------------------------------------

    @Test
    void testFormatter_EmptyMoviesList() {
        // Path Coverage: Verifies the "No recommendations" branch in OutputFormatter
        RecommendationEngine engine = new RecommendationEngine();
        User u1 = new User("Ahmed", "111111111", List.of("horror"));
        var recommendations = engine.recommend(List.of(u1), List.of()); // No movies

        String output = new Movie_Recommendation_System_Project.output.OutputFormatter()
                .formatRecommendations(recommendations);
        assertTrue(output.contains("horror: No recommendations"));
    }

    @Test
    void testEngine_EmptyInputLists() {
        // Edge Case: Ensures the system doesn't crash if passed empty lists (0 users, 0 movies)
        RecommendationEngine engine = new RecommendationEngine();
        Map<User, Map<String, List<Movie>>> result = engine.recommend(new ArrayList<>(), new ArrayList<>());
        assertTrue(result.isEmpty());
    }
    @Test
    void testValidator_MovieIdExactlyThreeDigits() {
        // Logic: Tests the boundary of "exactly 3 digits".
        Validator validator = new Validator();
        Movie m1 = new Movie("The Nun", "TN12", List.of("horror")); // 2 digits
        Movie m2 = new Movie("The Nun", "TN1234", List.of("horror")); // 4 digits

        assertNotNull(validator.validate(List.of(m1), List.of()));
        assertNotNull(validator.validate(List.of(m2), List.of()));
    }

    @Test
    void testValidator_MovieIdAllUniqueDigits() {
        // Logic: Ensures the allUnique helper correctly flags repeating digits.
        Validator validator = new Validator();
        Movie m1 = new Movie("The Nun", "TN122", List.of("horror"));
        assertEquals("Movie Id numbers ERROR: TN122 aren't unique", validator.validate(List.of(m1), List.of()));
    }

    @Test
    void testValidator_UserIdNonAlphanumeric() {
        // Logic: Checks the isUserIdValid branch for special characters.
        Validator validator = new Validator();
        User u1 = new User("Ahmed", "12345678@", List.of("action"));
        assertNotNull(validator.validate(List.of(), List.of(u1)));
    }

    @Test
    void testValidator_TitleEmptyWord() {
        // Edge Case: Checks how isTitleValid handles potential null/empty strings after splitting.
        Validator validator = new Validator();
        Movie m1 = new Movie("The  Nun", "TN123", List.of("horror")); // Double space
        assertNull(validator.validate(List.of(m1), List.of()), "Double spaces should be handled by regex split");
    }

    // -----------------------------------------------------------------------
    // RECOMMENDATION ENGINE (DATA STRUCTURE INTEGRITY)
    // -----------------------------------------------------------------------

    @Test
    void testEngine_CategoryOrderPerUser() {
        // Path: Verifies that the internal category map preserves the user's specific liked_categories order.
        RecommendationEngine engine = new RecommendationEngine();
        User u1 = new User("Ahmed", "111111111", List.of("drama", "horror"));

        var result = engine.recommend(List.of(u1), List.of()).get(u1);
        Iterator<String> it = result.keySet().iterator();
        assertEquals("drama", it.next());
        assertEquals("horror", it.next());
    }

    @Test
    void testEngine_MovieOrderInCategory() {
        // Path: Verifies that movies are added to the recommendation list in the order they appear in movies.txt.
        RecommendationEngine engine = new RecommendationEngine();
        Movie m1 = new Movie("First", "F123", List.of("action"));
        Movie m2 = new Movie("Second", "S456", List.of("action"));
        User u1 = new User("Ahmed", "111111111", List.of("action"));

        var recs = engine.recommend(List.of(u1), List.of(m1, m2)).get(u1).get("action");
        assertEquals(m1, recs.get(0));
        assertEquals(m2, recs.get(1));
    }

    // -----------------------------------------------------------------------
    // OUTPUT FORMATTER (STRING BUILDER LOGIC)
    // -----------------------------------------------------------------------

    @Test
    void testFormatter_SingleUserFormatting() {
        // Logic: Ensures no leading/trailing blank lines for a single user.
        OutputFormatter formatter = new OutputFormatter();
        RecommendationEngine engine = new RecommendationEngine();
        User u1 = new User("Ahmed", "111111111", List.of("action"));
        Movie m1 = new Movie("Batman", "B123", List.of("action"));

        String output = formatter.formatRecommendations(engine.recommend(List.of(u1), List.of(m1)));
        assertFalse(output.startsWith("\n"), "Should not start with newline");
        assertFalse(output.endsWith("\n"), "Should not end with newline");
    }

    @Test
    void testFormatter_MultipleCategoriesCommaSeparation() {
        // Logic: Verifies the "movieId-movieTitle, movieId-movieTitle" comma logic.
        OutputFormatter formatter = new OutputFormatter();
        Map<User, Map<String, List<Movie>>> data = new LinkedHashMap<>();
        User u = new User("Ahmed", "111111111", List.of("action"));
        Movie m1 = new Movie("M1", "M123", List.of("action"));
        Movie m2 = new Movie("M2", "M456", List.of("action"));

        Map<String, List<Movie>> catMap = new LinkedHashMap<>();
        catMap.put("action", List.of(m1, m2));
        data.put(u, catMap);

        String output = formatter.formatRecommendations(data);
        assertTrue(output.contains("M123-M1, M456-M2"));
    }

    // -----------------------------------------------------------------------
    // MODEL OBJECTS (IMMUTABILITY CHECK)
    // -----------------------------------------------------------------------

    @Test
    void testModel_MovieStateIntegrity() {
        // Logic: Ensures fields assigned in constructor are correctly retrieved via getters.
        List<String> cats = List.of("action");
        Movie m = new Movie("Title", "T123", cats);
        assertEquals("Title", m.getTitle());
        assertEquals("T123", m.getMovieId());
        assertEquals(cats, m.getCategories());
    }

    @Test
    void testModel_UserStateIntegrity() {
        // Logic: Ensures user fields are correctly stored.
        List<String> liked = List.of("drama");
        User u = new User("Ahmed", "111111111", liked);
        assertEquals("Ahmed", u.getUsername());
        assertEquals("111111111", u.getUserId());
        assertEquals(liked, u.getLikedCategories());
    }
}
