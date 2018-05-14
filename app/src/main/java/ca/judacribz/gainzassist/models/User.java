package ca.judacribz.gainzassist.models;

public class User {

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private String email;
    private String uid;
    private static User instance;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // ThumbnailAdapter Constructor/Instance                                                     //
    // ######################################################################################### //
    private User() {
    }

    /* Returns the instance if one exists, otherwise creates one and returns it */
    public static User getInstance(){
        if (instance == null) {
            instance = new User();
        }

        return instance;
    }
    // ######################################################################################### //

    // Getters and setters
    // ============================================================================================
    public void setEmail(String email) {
        this.email = email;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public String getUid() {
        return uid;
    }
    // ============================================================================================
}
