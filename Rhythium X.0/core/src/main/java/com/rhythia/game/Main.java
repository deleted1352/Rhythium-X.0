package com.rhythia.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.rhythia.game.screens.MenuScreen;

public class Main extends Game {
    public SpriteBatch batch;
    public Texture solidBackground;
    public BitmapFont font;

    @Override
//    public void create() {
//        batch = new SpriteBatch();
//        font = new BitmapFont();
//
//        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
//
//        pixmap.setColor(Color.valueOf("#333333"));
//        pixmap.fill();
//        solidBackground = new Texture(pixmap);
//        pixmap.dispose();
//        this.setScreen(new MenuScreen(this));
//    }
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        this.setScreen(new MenuScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        solidBackground.dispose();
    }
}
