package com.rhythia.game.screens;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.rhythia.game.Main;

public class SettingsScreen extends ScreenAdapter{
    private Main game;
    private MenuScreen menuScreen;
    private Vector3 touchPoint;
    private ArrayList<Object> colorPatterns;
    private ArrayList<Rectangle> colorBounds;
    private ArrayList<Rectangle> diffBounds;
    private static String selectedC = "Standard";
    private static String difficulty = "Easy";

    private ShapeRenderer shapeRenderer;
    private int gridLength = 1200;
    private int gridWidth = 600;
    private int gridX, gridY;
    //TODO figure out if APCSA classroom computers have mouse sensitivity settings, otherwise add here
    //TODO design shop/quest screen
    
    /**
     * Creates a setting screen providing options on: difficulty, color patterns
     * Difficulty: time until hit
     * @param game
     * @param menuScreen
     */
    public SettingsScreen(Main game, MenuScreen menuScreen)
    {
        this.game = game;
        this.menuScreen = menuScreen;
        this.shapeRenderer = new ShapeRenderer();
        this.touchPoint = new Vector3();
        colorPatterns = new ArrayList<>();
        colorBounds = new ArrayList<>();
        diffBounds = new ArrayList<>();
        initializeColor();
    }
    
    @Override
    public void show()
    {
        gridX = (Gdx.graphics.getWidth() - gridLength) / 2;
        gridY = (Gdx.graphics.getHeight() - gridWidth) / 2;

        for (int i = 0; i < 3; i++) {
            diffBounds.add(new Rectangle(gridX + 700, gridY + gridWidth - 150 - 30 * (i + 1), 200, 25));
        }
    }

    @Override
    public void render(float delta)
    {
        // Gdx.gl.glClearColor(0.01f, 0.01f, 0.01f, 1);
        // Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {   
            game.setScreen(new MenuScreen(game));
            return;
        }

        // handleMouseLocking();

        // draw grid for screen
        Rectangle r1 = new Rectangle(gridX - (2 * 0.5f), gridY - (2 * 0.5f), gridLength + 2, gridWidth + 2);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK); //1,1,1,1
        for (int i = 0; i < 3; i++) {
            shapeRenderer.rect(gridX - (i * 0.5f), gridY - (i * 0.5f), gridLength + i, gridWidth + i);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeType.Line);
        
        
        game.batch.begin();
        game.font.getData().setScale(0.25f);
        for(int i = 0; i < colorPatterns.size(); i += 2) {
            game.font.draw(game.batch, (String) colorPatterns.get(i), gridX + 50, gridY + gridWidth - 150 - 20 * (i));
        }
        game.font.draw(game.batch, "Easy", gridX + 700, gridY + gridWidth - 150 - 30 * (0));
        game.font.draw(game.batch, "Medium", gridX + 700, gridY + gridWidth - 150 - 30 * (1));
        game.font.draw(game.batch, "Hard", gridX + 700, gridY + gridWidth - 150 - 30 * (2));
        game.batch.end();

        // shapeRenderer.end();
        
        // shapeRenderer.begin();

        // shapeRenderer.setColor(Color.BLUE);
        // for (Rectangle r : colorBounds) {
        //     shapeRenderer.rect(r.x, r.y, r.width, r.height);
        // }
        shapeRenderer.end();

        // handle input
        if (Gdx.input.justTouched()) {
            touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            float flippedY = Gdx.graphics.getHeight() - touchPoint.y;
            
            if (!r1.contains(touchPoint.x, flippedY)) {
                game.setScreen(new MenuScreen(game));
                return;
            }
            
            // check which theme was selected
            int i = 1;
            for (Rectangle r : colorBounds) {
                if (r.contains(touchPoint.x, flippedY)) {
                    MenuScreen.colorTheme = (float[]) colorPatterns.get(i);
                    selectedC = (String) colorPatterns.get(i-1);
                }
                i+=2;
            }

            int j = 0;
            for (Rectangle r : diffBounds) {
                if (r.contains(touchPoint.x, flippedY)) {
                    if (j == 0) difficulty = "Easy";
                    if (j == 1) difficulty = "Medium";
                    if (j == 2) difficulty = "Hard";
                    MenuScreen.difficulty = difficulty;
                }
                j++;

            }
        }

        game.batch.begin();
        game.font.getData().setScale(0.5f);
        game.font.draw(game.batch, "Color Theme: " + selectedC, gridX + 50, gridY + gridWidth - 50);
        game.font.draw(game.batch, "Difficulty: " + difficulty, gridX + 700, gridY + gridWidth - 50);
        game.batch.end();
    }
        // private void handleMouseLocking() {
    //     int mx = Gdx.input.getX();
    //     int my = Gdx.input.getY();
    //     float screenGridTop = Gdx.graphics.getHeight() - (gridY + gridLength);
    //     float screenGridBottom = Gdx.graphics.getHeight() - gridY;
    //     int wx = mx, wy = my;
    //     boolean warp = false;
    //     if (mx < gridX) { wx = (int)gridX; warp = true; }
    //     if (mx > gridX + gridWidth) { wx = (int)(gridX + gridWidth); warp = true; }
    //     if (my < screenGridTop) { wy = (int)screenGridTop; warp = true; }
    //     if (my > screenGridBottom) { wy = (int)screenGridBottom; warp = true; }
    //     if (warp) Gdx.input.setCursorPosition(wx, wy);
    // }

    // private void handleSweeperInput() {
    //     float mx = Gdx.input.getX();
    //     float my = Gdx.graphics.getHeight() - Gdx.input.getY();
    //     for (Rectangle r : colorBounds) {
    //         if (r.contains(mx, my)) {
    //             //
    //         }
    //     }
    // }
     private void initializeColor()
    {
        float[] standard = {325, 0.15f, 296, 0.50f, 273, 0.84f, 273, 1.00f, 241, 0.98f, 180, 1, 145, 0.60f, 55, 0.80f, 48,0.80f};
        //{180, 0.9f, 200, 0.8f, 220, 0.9f, 160, 0.7f}
        float[] ocean = {180, 0.90f, 195, 0.85f, 210, 0.90f, 225, 0.88f, 165, 0.80f, 200, 0.75f, 240, 0.85f};
        // {25, 0.95f, 45, 0.9f, 330, 0.85f, 355, 0.9f}
        float[] sunset = {25, 0.95f, 40, 0.90f, 55, 0.85f, 330, 0.88f, 350, 0.92f, 10, 0.90f, 300, 0.85f};
        float[] nature = {65f, 0.90f,   80f, 0.88f,   95f, 0.92f, 110f, 0.90f,  130f, 0.85f,  155f, 0.82f, 175f, 0.80f};
        float[] space = {250f, 0.95f, 260f, 0.92f, 270f, 0.90f, 280f, 0.88f, 290f, 0.90f, 300f, 0.92f, 310f, 0.88f};
        float[] sun = {35, 0.95f, 45, 0.92f, 55, 0.88f, 25, 0.90f, 15, 0.85f, 5, 0.90f, 60, 0.80f};
        // float[] autumn = {5, 0.95f, 15, 0.90f, 25, 0.85f, 10, 0.88f, 350, 0.92f, 30, 0.80f, 0, 0.90f};
        float[] cottonCandy = {325f, 0.90f, 335f, 0.88f, 345f, 0.85f, 355f, 0.82f, 310f, 0.80f, 300f, 0.78f, 320f, 0.75f};
        // float[] crystal = {185f, 0.70f, 195f, 0.75f, 205f, 0.78f, 215f, 0.80f, 225f, 0.75f, 235f, 0.72f, 245f, 0.70f};
        float[] mist = {170f, 0.80f, 180f, 0.82f, 190f, 0.85f, 200f, 0.88f, 210f, 0.85f, 160f, 0.78f, 220f, 0.80f};
        // float[] amethyst = {265f, 0.95f, 275f, 0.92f, 285f, 0.90f, 295f, 0.88f, 305f, 0.90f, 315f, 0.92f, 325f, 0.88f};
        colorPatterns.add("Standard");
        colorPatterns.add(standard);
        colorPatterns.add("Oceanic");
        colorPatterns.add(ocean);
        colorPatterns.add("Sunset");
        colorPatterns.add(sunset);
        colorPatterns.add("Nature");
        colorPatterns.add(nature);
        colorPatterns.add("Space");
        colorPatterns.add(space);
        colorPatterns.add("Sun");
        colorPatterns.add(sun);
        // colorPatterns.add("Autumn");
        // colorPatterns.add(autumn);
        colorPatterns.add("Cotton Candy");
        colorPatterns.add(cottonCandy);
        // colorPatterns.add("Crystal");
        // colorPatterns.add(crystal);
        colorPatterns.add("Mist");
        colorPatterns.add(mist);
        // colorPatterns.add("Amethyst");
        // colorPatterns.add(amethyst);
        float gx =(Gdx.graphics.getWidth() - gridLength) / 2;
        float gy = (Gdx.graphics.getHeight() - gridWidth) / 2;
        for(int i = 0; i < colorPatterns.size(); i += 2) {
            colorBounds.add(new Rectangle(gx + 50, gy + gridWidth - 150 - 20 * (i + 1), 200, 25));
        }
    }
    
    public void playColors(int index)
    {
        //
    }
}