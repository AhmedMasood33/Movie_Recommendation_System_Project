package Movie_Recommendation_System_Project.model;

import java.util.List;

public class Movie {
    
    private final String title;
    private final String movieId;
    private final List<String> categories;

    public Movie(String title, String movieId, List<String> categories) {
        this.title = title;
        this.movieId = movieId;
        this.categories = categories;
    }

    public String getTitle() {
        return title;
    }

    public String getMovieId() {
        return movieId;
    }

    public List<String> getCategories() {
        return categories;
    }
}
