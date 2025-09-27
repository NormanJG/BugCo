package com.cab302.bugco;

public class Players {
    private String username;
    private String achievement;
    private int points;   // new

    // keep old 2-arg ctor for existing code paths
    public Players(String username, String achievement) {
        this(username, achievement, 0);
    }

    public Players(String username, String achievement, int points) {
        this.username = username;
        this.achievement = achievement;
        this.points = points;
    }

    public String getUsername() { return username; }
    public String getAchievement() { return achievement; }
    public void setAchievement(String achievement) { this.achievement = achievement; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}
