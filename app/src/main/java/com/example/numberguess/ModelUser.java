package com.example.numberguess;

public class ModelUser {
    private String userName;
    private String password;
    private int score;
    private long id;

    public ModelUser(String userName) {
        this.userName = userName;
        this.password = "";
        this.score = 0;
        this.id = 0;
    }

    public ModelUser(String userName, int score, long id) {
        this.userName = userName;
        this.password = "";
        this.score = score;
        this.id = id;
    }

    public ModelUser(String userName, String password, int score, long id) {
        this.userName = userName;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int s) {
        this.score = s;
    }
}