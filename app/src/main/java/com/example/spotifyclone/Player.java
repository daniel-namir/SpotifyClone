package com.example.spotifyclone;


import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Player implements Serializable {
    private static SpotifyAppRemote remote;
    private static TreeMap<String, HashMap<String, String>> songs;
    private static String token;

    public Player(SpotifyAppRemote remote, String token) {
        this.remote = remote;
        songs = new TreeMap<>();
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }

    public void addSong(String name, HashMap info) {
        songs.put(name, info);
    }

    public ArrayList<String> getSongs() {
        ArrayList<String> songList = new ArrayList<>();
        for (String song : songs.keySet()) {
            songList.add(song);
        }
        return songList;
    }

    public void removeSong(String name) {
        songs.remove(name);
    }

    public void playSong(String name) {
        remote.getPlayerApi().play(songs.get(name).get("uri"));
    }

    // TODO: If a song is finished play the next song
}