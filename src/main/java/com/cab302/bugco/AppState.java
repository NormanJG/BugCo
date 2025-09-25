package com.cab302.bugco;

// keep small app info here
public final class AppState {
    public static final AppState ME = new AppState();

    public String userName = "Guest";
    public boolean loggedIn = false;

    private AppState() {}

    // set user after login
    public void loginAs(String theName) {
        this.userName = theName;
        this.loggedIn = true;
    }

    // clear user
    public void logoutNow() {
        this.userName = "Guest";
        this.loggedIn = false;
    }

    public boolean isLoggedIn() { return loggedIn; }
}
