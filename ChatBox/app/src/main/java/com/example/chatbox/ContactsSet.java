package com.example.chatbox;

public class ContactsSet {
    private String username,image,about;

    public ContactsSet(){

    }

    public ContactsSet(String username, String image, String about) {
        this.username = username;
        this.image = image;
        this.about = about;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}
