package com.rhythia.game.screens;

import java.util.TreeSet;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.rhythia.game.Main;

public class MenuScreen extends ScreenAdapter {
    private final Main game;
    private TreeSet<SongEntry> songs;
    private ArrayList<Rectangle> songBounds;
    private Vector3 touchPoint;
    private final float BUTTON_HEIGHT = 80;
    private final float BUTTON_WIDTH = 800;
    private final float START_Y = 200;
    private final float PADDING = 40;
    private Rectangle uploadButton;
    private UploadScreen uploadScreen;
    public MenuScreen(Main game) {
        this.game = game;
        this.songs = new TreeSet<>();
        this.songBounds = new ArrayList<>();
        this.touchPoint = new Vector3();
        this.uploadScreen = new UploadScreen(game, this);
        // initialize default songs
        songs.add(new SongEntry("NEVADA - VICETONE", "nevada.mp3", "nevada.txt"));
    }

    public void addSong(SongEntry song) {
        songs.add(song);
        updateSongBounds();
    }

    public void removeSong(SongEntry song) {
        songs.remove(song);
        updateSongBounds();
    }

    public TreeSet<SongEntry> getSongs() {
        return songs;
    }

    
    private void updateSongBounds() {
        songBounds.clear();
        int index = 0;
        for (SongEntry song : songs) {
            float yPos = START_Y + (index * (BUTTON_HEIGHT + PADDING));
            Rectangle bounds = new Rectangle(100, Gdx.graphics.getHeight() - yPos - BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT);
            songBounds.add(bounds);
            index++;
        }
    }

    @Override
    public void show() {
        updateSongBounds();
        uploadButton = new Rectangle(
        100,
        40,
        300,
        60
    );
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        
        // Draw title
        game.font.getData().setScale(1);
        game.font.draw(game.batch, "RHYTHIUM", 100, Gdx.graphics.getHeight() - 50);

        // Draw all songs
        game.font.getData().setScale(0.6f);
        int index = 1;
        System.out.println(songs);
        for (SongEntry song : songs) {
            float yPos = START_Y + ((index - 1) * (BUTTON_HEIGHT + PADDING));
            game.font.draw(game.batch, index + ". " + song.title, 120, Gdx.graphics.getHeight() - yPos + 30);
            index++;
        }

        
        game.font.getData().setScale(0.4f);
        game.font.draw(game.batch, "[UPLOAD SONG]", 100, 60);
        //game.font.draw(game.batch, "Tap a song to play", 100, 100);
        

        


        game.batch.end();

        // handle input
        if (Gdx.input.justTouched()) {
            touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            float flippedY = Gdx.graphics.getHeight() - touchPoint.y;

            // check which song was clicked
            int index2 = 0;
            for (SongEntry song : songs) {
                if (songBounds.get(index2).contains(touchPoint.x, flippedY)) {
                    game.setScreen(new GameplayScreen(game, song));
                    System.out.println(song);
                    return;
                }
                index2++;
            }

            game.setScreen(uploadScreen);
        }
    }
}