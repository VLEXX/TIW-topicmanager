package it.beans;

public class UserBean {

    private String username;
    private	String hash;

    public UserBean(String username, String hash) {
        this.username = username;
        this.hash = hash;
    }

    public String getUsername() {
        return this.username;
    }

    public String getHash() {
        return hash;
    }
}