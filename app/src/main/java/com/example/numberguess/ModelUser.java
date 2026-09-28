package com.example.numberguess;

public class ModelUser {
    private String userName; // Account login username
    private String name;     // Player display name
    private String password;
    private int score;
    private long id;

    public ModelUser(String userName, String name) {
        this.userName = userName;
        this.name = name;
        this.password = "";
        this.score = 0;
        this.id = 0;
    }

    public ModelUser(String userName, String name, String password, int score, long id) {
        this.userName = userName;
        this.name = name;
        this.password = password;
        this.score = score;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}