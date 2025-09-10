package com.cab302.bugco;

public final class AppState {
    public static final AppState ME = new AppState();

    public String userName = "Guest";
    public boolean loggedIn = false;

    private AppState() {}

    public boolean isLoggedIn() { return loggedIn; }
    public void loginAs(String name) {
        this.userName = name; this.loggedIn = true;
    }
    public void logout() {
        this.userName = "Guest"; this.loggedIn = false;
    }
}
