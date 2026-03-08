package Movie_Recommendation_System_Project.model;

import java.util.List;

public class User {
    private final String username;
    private final String userId;
    private final List<String> likedCategories;

    public User(String username, String userId, List<String> likedCategories) {
        this.username = username;
        this.userId = userId;
        this.likedCategories = likedCategories;
    }

    public String getUsername() {
        return username;
    }

    public String getUserId() {
        return userId;
    }

    public List<String> getLikedCategories() {
        return likedCategories;
    }
}
