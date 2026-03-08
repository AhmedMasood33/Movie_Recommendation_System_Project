package com.movierecommend.engine;

import com.movierecommend.model.Movie;
import com.movierecommend.model.User;

import java.util.*;

/**
 * Generates movie recommendations for each user based on their liked categories.
 */
public class RecommendationEngine {

    /**
     * Builds a recommendation map for all users.
     *
     * @param users  validated list of users
     * @param movies validated list of movies
     * @return ordered map: User -> (ordered map: category -> list of matching movies)
     */
    public Map<User, Map<String, List<Movie>>> recommend(List<User> users, List<Movie> movies) {
        // Use LinkedHashMap to preserve insertion order
        Map<User, Map<String, List<Movie>>> result = new LinkedHashMap<>();

        for (User user : users) {
            Map<String, List<Movie>> categoryMap = new LinkedHashMap<>();

            for (String likedCategory : user.getLikedCategories()) {
                List<Movie> matches = new ArrayList<>();
                for (Movie movie : movies) {
                    if (movie.getCategories().contains(likedCategory)) {
                        matches.add(movie);
                    }
                }
                categoryMap.put(likedCategory, matches);
            }

            result.put(user, categoryMap);
        }

        return result;
    }
}
