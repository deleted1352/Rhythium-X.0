package com.rhythia.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.rhythia.game.Main;

public class MenuScreen extends ScreenAdapter {
    private final Main game;

    // nevada clickable
    private Rectangle nevadaBounds;
    private Vector3 touchPoint;

    public MenuScreen(Main game) {
        this.game = game;
        nevadaBounds = new Rectangle(100, Gdx.graphics.getHeight() - 150, 600, 100);
        touchPoint = new Vector3();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.font.getData().setScale(1);
        game.font.draw(game.batch, "1. NEVADA - VICETONE", 100, Gdx.graphics.getHeight() - 100);

        game.font.getData().setScale(0.5f);
        game.font.draw(game.batch, "Tap song title to play", 100, 200);
        game.batch.end();

        if (Gdx.input.justTouched()) {
            touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            float flippedY = Gdx.graphics.getHeight() - touchPoint.y;

            if (nevadaBounds.contains(touchPoint.x, flippedY)) {
                game.setScreen(new GameplayScreen(game));
            }
        }
    }
    /*
    public void render(float delta) {
        // Just a blue screen to prove it works
        Gdx.gl.glClearColor(0, 0, 0.5f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
     */
}
