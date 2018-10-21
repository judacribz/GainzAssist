package ca.judacribz.gainzassist.models;

import java.util.ArrayList;

public class CurrUser {

    // Constants
    // --------------------------------------------------------------------------------------------
    private static final CurrUser INST = new CurrUser();
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private String email;
    private String uid;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // ThumbnailAdapter Constructor/Instance                                                     //
    // ######################################################################################### //
    private CurrUser() {
    }

    public static CurrUser getInstance(){
        return INST;
    }
    // ######################################################################################### //

    // Getters and setters
    // ============================================================================================
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
    // ============================================================================================
}
