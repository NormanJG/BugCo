package com.cab302.bugco;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public final class MusicService {
    private static MediaPlayer current;
    private static MediaPlayer loginPlayer;
    private static MediaPlayer homePlayer;

    private MusicService() {}

    public static void init() {
        loginPlayer = build("music.wav");
        homePlayer  = build("home.wav");
    }

    private static MediaPlayer build(String fileName) {
        var url = MusicService.class.getResource(fileName);
        if (url == null) return null;
        var mp = new MediaPlayer(new Media(url.toExternalForm()));
        mp.setCycleCount(MediaPlayer.INDEFINITE);
        mp.setVolume(0.05);
        return mp;
    }

    private static void switchTo(MediaPlayer next) {
        if (current != null) current.stop();
        current = next;
        if (current != null) current.play();
    }

    public static void playLogin() { switchTo(loginPlayer); }
    public static void playHome()  { switchTo(homePlayer);  }

    public static void dispose() {
        if (current != null) current.dispose();
        if (loginPlayer != null) loginPlayer.dispose();
        if (homePlayer != null)  homePlayer.dispose();
        current = loginPlayer = homePlayer = null;
    }
    public static void play() {
        if (current != null) current.play();
    }
    public static void pause() {
        if (current != null) current.pause();
    }
    public static void toggle() {
        if (current == null) return;
        switch (current.getStatus()) {
            case PLAYING -> current.pause();
            default       -> current.play();
        }
    }
    public static void setMute(boolean mute) {
        if (current != null) current.setMute(mute);
    }
    public static boolean isMute() {
        return current != null && current.isMute();
    }
    public static void setVolume(double v) {   // 0.0 - 1.0
        if (current != null) current.setVolume(v);
    }
    public static double getVolume() {
        return current != null ? current.getVolume() : 0.0;
    }
}
