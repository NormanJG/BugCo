package com.cab302.bugco;

public final class GameState {
    private String myName;
    private String theDifficulty;
    private int theScoreNow;

    public GameState(String myName) {
        this.myName = myName == null ? "Guest" : myName.trim();
        this.theDifficulty = "Easy";
        this.theScoreNow = 0;
    }

    public String getMyName() { return myName; }
    public void setMyName(String theUpdated) { this.myName = theUpdated; }

    public String getTheDifficulty() { return theDifficulty; }
    public void setTheDifficulty(String d) { this.theDifficulty = d; }

    public int getTheScoreNow() { return theScoreNow; }
    public void setTheScoreNow(int s) { this.theScoreNow = s; }
    public void addScore(int more) { this.theScoreNow += Math.max(0, more); }
}
