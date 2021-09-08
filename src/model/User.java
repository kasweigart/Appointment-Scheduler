package model;

/**
 * This class is the model for a user.
 */
public class User {

    /**
     * two private members create a user object
     * instance is used to determine which user is logged in
     */
    private static User instance = null;
    private int userId;
    private static String username;

    /**
     * @param userId unique ID of a user
     * @param username unique name of a user
     */
    private User(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    /**
     * @return returns an instance of a user
     *
     */
    public static User getInstance() {
        if (instance == null) {
            instance = new User(0,"username");
        }
        return instance;
    }

    /**
     * @return returns the ID of a user
     */
    public int getUserId() {
        return userId;
    }

    /**
     * @return returns the name of a user
     */
    public static String getUsername() {
        return username;
    }

    /**
     * @param userId sets the ID of a user
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * @param username sets the name of a user
     */
    public void setUsername(String username) {
        this.username = username;
    }
}
