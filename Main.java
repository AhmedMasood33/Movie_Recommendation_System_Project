package Movie_Recommendation_System_Project;

import Movie_Recommendation_System_Project.engine.RecommendationEngine;
import Movie_Recommendation_System_Project.model.Movie;
import Movie_Recommendation_System_Project.model.User;
import Movie_Recommendation_System_Project.output.OutputFormatter;
import Movie_Recommendation_System_Project.parser.FileParser;
import Movie_Recommendation_System_Project.validation.Validator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class Main {

    private static final String OUTPUT_FILE = "output.txt";

    public static void main(String[] args) {
        String moviesFile = (args.length > 0) ? args[0] : "movies.txt";
        String usersFile  = (args.length > 1) ? args[1] : "users.txt";

        FileParser parser = new FileParser();
        List<Movie> movies;
        List<User>  users;

        // --- Parse files ---
        try {
            movies = parser.parseMovies(moviesFile);
        } catch (IOException e) {
            System.err.println("Error reading movies file: " + e.getMessage());
            return;
        }

        try {
            users = parser.parseUsers(usersFile);
        } catch (IOException e) {
            System.err.println("Error reading users file: " + e.getMessage());
            return;
        }

        // --- Validate ---
        Validator validator = new Validator();
        String error = validator.validate(movies, users);

        if (error != null) {
            writeOutput(error);
            return;
        }

        // --- Generate recommendations ---
        RecommendationEngine engine = new RecommendationEngine();
        Map<User, Map<String, List<Movie>>> recommendations = engine.recommend(users, movies);

        // --- Format and write output ---
        OutputFormatter formatter = new OutputFormatter();
        writeOutput(formatter.formatRecommendations(recommendations));
    }

    /**
     * Prints the given text to the console and writes it to output.txt.
     */
    private static void writeOutput(String text) {
        System.out.println(text);
        try (PrintWriter pw = new PrintWriter(new FileWriter(OUTPUT_FILE))) {
            pw.println(text);
            System.out.println("\n[Output also saved to " + OUTPUT_FILE + "]");
        } catch (IOException e) {
            System.err.println("Warning: could not write to " + OUTPUT_FILE + ": " + e.getMessage());
        }
    }
}

