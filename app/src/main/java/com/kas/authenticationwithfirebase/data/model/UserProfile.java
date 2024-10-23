package com.kas.authenticationwithfirebase.data.model;

public class UserProfile {

    private String email;
    private String imageUrl;

    public UserProfile(String email, String imageUrl) {
        this.email = email;
        this.imageUrl = imageUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
