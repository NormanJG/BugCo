package com.cab302.bugco;

public class Players {
    private String username;
    private String achievement;

    public Players(String username, String achievement) {
        this.username = username;
        this.achievement = achievement;
    }

    public String getUsername() {
        return username;
    }

    public String getAchievement() {
        return achievement;
    }

    public void setAchievement(String achievement) {
        this.achievement = achievement;
    }
}
