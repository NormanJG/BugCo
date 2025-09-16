package com.cab302.bugco;

public final class Session {
    private static String currentUser;

    private Session() {} // no instances

    public static void setCurrentUser(String username) { currentUser = username; }
    public static String getCurrentUser() { return currentUser; }
    public static boolean isLoggedIn() { return currentUser != null; }
    public static void logout() { currentUser = null; }
}
