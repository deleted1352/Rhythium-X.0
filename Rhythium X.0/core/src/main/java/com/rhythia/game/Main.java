package com.rhythia.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

//import javafx.application.Platform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.rhythia.game.screens.MenuScreen;
import com.rhythia.game.screens.UploadScreen;


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
        //Platform.startup(() -> {}); 
        batch = new SpriteBatch();
        font = new BitmapFont();
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/abel-regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter params =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        params.size = 96;              // ← change this number to adjust size
        params.color = Color.WHITE;    // text color
        params.borderWidth = 3f;
        params.borderColor = Color.WHITE;

        params.minFilter = Texture.TextureFilter.Linear;
        params.magFilter = Texture.TextureFilter.Linear;
        params.gamma = 1.8f;
        params.borderGamma = 1.8f;


        font = generator.generateFont(params);
        generator.dispose();
        
        MenuScreen menuScreen = new MenuScreen(this);
        
        //this.setScreen(new UploadScreen(this, menuScreen));
        this.setScreen(menuScreen);
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
        Gdx.app.exit();
    }
}
