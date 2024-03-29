/*
 * BrowsePageFragment.java
 * @author: Daniel and Kelvin
 *
 * This program sets up the fragment for the browse page fragment. The user will be given 4 random
 * choices for genres to search from. The user can click randomize and they will get 4 more choices
 * of generes. Once a user clicks a genre the display will change and a list of 50 songs will be
 * displayed of the genre. These songs were gathered using an API call to the Spotify API. The user
 * can click one of these songs and it will start playing and add it to the library of songs in the
 * user's playlist. The user can click back to go back and select another genre to search from.
 *
 */

package com.example.spotifyclone;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Allows the user 4 choices for a genre search. The user can click randomize to get
 * additional choices
 */
public class BrowsePageFragment extends Fragment {

    private static List<String> genres = new ArrayList<>();
    private static List<String> colors = new ArrayList<>();
    private static List<String> songListName = new ArrayList<String>();
    private static HashMap<String, HashMap<String, String>> songMap = new HashMap<>();
    private static String token;
    private static Player player;
    private static View view;

    /**
     * Required empty public constructor
     */
    public BrowsePageFragment() { }

    /**
     * Sets the view for the browse page fragment
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        container.removeAllViews();
        view = inflater.inflate(R.layout.fragment_browse_page, container, false);
        Bundle bundle = this.getArguments();
        player = (Player) bundle.get("player");
        player.act = view;
        player.setNowPlaying();
        token = player.getToken();

        setGenres();
        setColors();
        setNavButtons();
        setGenreButtons();

        return view;
    }

    /**
     * Sets nav buttons the user can click on
     */
    public void setNavButtons() {
        Button home = view.findViewById(R.id.homePageButton);
        Button browse = view.findViewById(R.id.browsePageButton);
        Button search = view.findViewById(R.id.searchPageButton);
        Button library = view.findViewById(R.id.myLibraryButton);
        setOnClick(home, new HomePageFragment());
        setOnClick(browse, new BrowsePageFragment());
        setOnClick(search, new SearchPageFragment());
        setOnClick(library, new MyLibraryPageFragment());
        LinearLayout ll = view.findViewById(R.id.nowPlayingText);
        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { openNowPlayingPage(v); }});
        Button playPause = view.findViewById(R.id.playPauseSongButton);
        if (player.playing) changePlay(playPause);
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { changePlay(playPause); }});
    }

    /**
     * Change what the play button looks like
     * @param button play pause button for now playing section
     */
    public void changePlay(Button button) {
        if (!player.songList.isEmpty()) {
            if (button.getText().equals("play")) {
                button.setText("pause");
                button.setBackgroundResource(R.drawable.pause_icon);
                player.resume();
            } else {
                button.setText("play");
                button.setBackgroundResource(R.drawable.play_icon);
                player.pause();
            }
            button.setTextScaleX(0);
        }
    }

    /**
     * Randomizes buttons buttons genere and button colors and sets button clicks of the buttons
     */
    public void setGenreButtons() {
        randomize();
        buttonClick(view.findViewById(R.id.genre1));
        buttonClick(view.findViewById(R.id.genre2));
        buttonClick(view.findViewById(R.id.genre3));
        buttonClick(view.findViewById(R.id.genre4));

        Button random = view.findViewById(R.id.randomize);
        Button back = view.findViewById(R.id.back);
        random.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { randomize(); }});
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { changeDisplay(); }});
    }

    /**
     * Randomizes the genres and genre button colors
     */
    public void randomize() {
        Collections.shuffle(genres);
        Collections.shuffle(colors);
        Button genre1 = view.findViewById(R.id.genre1);
        Button genre2 = view.findViewById(R.id.genre2);
        Button genre3 = view.findViewById(R.id.genre3);
        Button genre4 = view.findViewById(R.id.genre4);
        genre1.setText(genres.get(0));
        genre2.setText(genres.get(1));
        genre3.setText(genres.get(2));
        genre4.setText(genres.get(3));
        genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(colors.get(0))));
        genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(colors.get(1))));
        genre3.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(colors.get(2))));
        genre4.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(colors.get(3))));
    }

    /**
     * When a genre is clicked it gets the genre name and sets the API search URL to that genre
     * name. It gets the JSON object of songs of that genre and updates the UI to display the list
     * of songs.
     * @param genre
     */
    public void buttonClick(Button genre) {
        genre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String genreName = genre.getText().toString();
                String search = setSearch(genreName);
                Thread t = new Thread(() -> {
                    JSONObject songObj = getSongs(search);
                    if (songObj != null) getActivity().runOnUiThread(() -> updateUI(songObj));
                });
                t.start();
            }
        });
    }

    /**
     * Sets the search url given the users choice whether is is artist of song
     */
    public String setSearch(String genreName) {
        Random random = new Random();
        int offset = random.nextInt(1000);
        String search = "https://api.spotify.com/v1/search?q=genre%3A" + genreName  +
                "&type=track&market=NA&limit=50&offset=" + offset;
        return search;
    }

    /**
     * Parses the JSON object to the the song name, uri, picture, and artist of all the songs
     * in the object
     * @param songObj JSON Object from Spotify API call
     */
    public void updateUI(JSONObject songObj) {
        try {
            songListName.clear();
            JSONArray items = songObj.getJSONObject("tracks").getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject trackObj = items.getJSONObject(i);
                String name = trackObj.getString("name");
                songListName.add(name);
                String picture = trackObj.getJSONObject("album").getJSONArray("images").getJSONObject(2).getString("url");
                String artist = trackObj.getJSONArray("artists").getJSONObject(0).getString("name");
                String uri = trackObj.getString("uri");
                HashMap<String, String> info = new HashMap<>();
                info.put("uri", uri);
                info.put("picture", picture);
                info.put("artist", artist);
                songMap.put(name, info);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        setList();
        changeDisplay();
    }

    /**
     * Sets list for the list of songs.
     */
    public void setList() {
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(getContext(), R.layout.song_row, songListName);
        ListView lw = getActivity().findViewById(R.id.browseSongs);
        lw.setAdapter(itemsAdapter);
        lw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = songListName.get(position);
                player.addSong(name, songMap.get(name));
            }
        });
        itemsAdapter.notifyDataSetChanged();
    }

    /**
     * Changes the display to the genre choices or the list of songs from the genere clicked
     */
    public void changeDisplay() {
        int visible1;
        int visible2;
        if (getActivity().findViewById(R.id.genre1).getVisibility() == View.GONE) {
            visible1 = View.VISIBLE;
            visible2 = View.GONE;
        } else {
            visible1 = View.GONE;
            visible2 = View.VISIBLE;
        }
        getActivity().findViewById(R.id.genre1).setVisibility(visible1);
        getActivity().findViewById(R.id.genre2).setVisibility(visible1);
        getActivity().findViewById(R.id.genre3).setVisibility(visible1);
        getActivity().findViewById(R.id.genre4).setVisibility(visible1);
        getActivity().findViewById(R.id.randomize).setVisibility(visible1);
        getActivity().findViewById(R.id.back).setVisibility(visible2);
        getActivity().findViewById(R.id.browseSongs).setVisibility(visible2);
    }

    /**
     * Gets all the generes from the genres.txt file and puts it in the generes arraylist
     */
    public void setGenres() {
        try {
            InputStream f = getActivity().getApplicationContext().getAssets().open("genres.txt");
            BufferedReader read = new BufferedReader(new InputStreamReader(f));
            String line = read.readLine();
            while (line != null) {
                genres.add(line);
                line = read.readLine();
            }
            read.close();
        } catch (FileNotFoundException e) {

            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets all the colors form the colors.txt file and puts it in the colors arraylist
     */
    public void setColors() {
        try {
            InputStream f = getActivity().getApplicationContext().getAssets().open("colors.txt");
            BufferedReader read = new BufferedReader(new InputStreamReader(f));
            String line = read.readLine();
            while (line != null) {
                String[] getHex = line.split("#");
                String hex = getHex[1];
                colors.add("#" + hex);
                line = read.readLine();
            }
            read.close();
        } catch (FileNotFoundException e) {

            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * send API call to Spotify API with the given search URL
     * @return JSON Object from API call
     */
    public JSONObject getSongs(String search) {
        try {
            String json = "";
            String line;
            URL object = new URL(search);
            HttpURLConnection con = (HttpURLConnection) object.openConnection();
            con.setDoInput(true);
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Authorization", "Bearer " + token);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while ((line = in.readLine()) != null) {
                json += line;
            }
            in.close();
            return new JSONObject(json);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Sets an on click listener to replace the search fragment with
     * @param btn Button to set Click Listener to
     * @param frag Fragment to replace search page layout with
     */
    public void setOnClick(Button btn, Fragment frag) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putSerializable("player", player);
                frag.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.browse_page_layout, frag)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    /**
     * Launches now playing fragment
     * @param v View
     */
    public void openNowPlayingPage(View v) {
        NowPlayingPageFragment nowPlayingPageFragment = new NowPlayingPageFragment();
        Bundle args = new Bundle();
        args.putSerializable("player", player);
        nowPlayingPageFragment.setArguments(args);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.browse_page_layout, nowPlayingPageFragment)
                .addToBackStack(null)
                .commit();
    }
}