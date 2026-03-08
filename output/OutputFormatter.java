package Movie_Recommendation_System_Project.output;

import Movie_Recommendation_System_Project.model.Movie;
import Movie_Recommendation_System_Project.model.User;

import java.util.List;
import java.util.Map;

public class OutputFormatter {
    

    /**
     * Formats the complete recommendation output for all users.
     *
     * Output per user:
     *   For User: username, user_id
     *   {category1}: movieId-movieTitle, movieId-movieTitle
     *   {category2}: ...
     *
     * A blank line separates different users.
     */
    public String formatRecommendations(Map<User, Map<String, List<Movie>>> recommendations) {
        StringBuilder sb = new StringBuilder();
        boolean firstUser = true;

        for (Map.Entry<User, Map<String, List<Movie>>> userEntry : recommendations.entrySet()) {
            if (!firstUser) {
                sb.append("\n");
            }
            firstUser = false;

            User user = userEntry.getKey();
            sb.append("For User: ")
              .append(user.getUsername())
              .append(", ")
              .append(user.getUserId())
              .append("\n");

            for (Map.Entry<String, List<Movie>> catEntry : userEntry.getValue().entrySet()) {
                String category = catEntry.getKey();
                List<Movie> movies = catEntry.getValue();

                sb.append(category).append(": ");

                if (movies.isEmpty()) {
                    sb.append("No recommendations");
                } else {
                    StringBuilder movieList = new StringBuilder();
                    for (int i = 0; i < movies.size(); i++) {
                        Movie m = movies.get(i);
                        if (i > 0) movieList.append(", ");
                        movieList.append(m.getMovieId())
                                 .append("-")
                                 .append(m.getTitle());
                    }
                    sb.append(movieList);
                }
                sb.append("\n");
            }
        }

        return sb.toString().stripTrailing();
    }
}
