package com.rhythia.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
// import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

//import javafx.application.Platform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.rhythia.game.screens.MenuScreen;

public class Main extends Game {
    public SpriteBatch batch;
    public Texture solidBackground;
    public BitmapFont font;

    /**
     * Initializes the game font and spritebatch, and sets the screen to a new Menuscreen
     */
    @Override
    public void create() {
        //Platform.startup(() -> {}); 
        batch = new SpriteBatch();
        font = new BitmapFont();
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/abel-regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter params =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        params.size = 96;
        params.color = Color.WHITE;
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

    /**
     * Calls Game class's render()
     */
    @Override
    public void render() {
        super.render();
    }

    /**
     * Calls Game class's dispose()
     */
    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        solidBackground.dispose();
        Gdx.app.exit();
    }
}
