package com.rhythia.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound; // New Import
import com.badlogic.gdx.graphics.GL20;
import com.rhythia.game.Main;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class GameplayScreen extends ScreenAdapter {
    private final Main game;
    private Music song;
    private Sound hitSound; // sound variable
    private float songTimer = 0.0f;
    private ShapeRenderer shapeRenderer;

    private Rectangle[] gridCells = new Rectangle[9];
    private Rectangle[] spawnCells = new Rectangle[9];
    private Array<Note> allNotes = new Array<>();

    private float gridSize = 600;
    private float gridX, gridY;

    // changeable settings
    private final float APPROACH_TIME = 1.4f;
    private final float FADE_IN_DURATION = 0.5f;
    private final float PUSHBACK = 0.94f;
    private final float SPAWN_SCALE = 0.10f;
    private final float HIT_WINDOW = 0.12f;

    public GameplayScreen(Main game) {
        this.game = game;
        this.shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
        Gdx.input.setCursorCatched(false);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        gridX = (Gdx.graphics.getWidth() - gridSize) / 2;
        gridY = (Gdx.graphics.getHeight() - gridSize) / 2;
        float cellSize = gridSize / 3;

        float spawnGridSize = gridSize * SPAWN_SCALE;
        float spawnX = (Gdx.graphics.getWidth() - spawnGridSize) / 2;
        float spawnY = (Gdx.graphics.getHeight() - spawnGridSize) / 2;
        float spawnCellSize = spawnGridSize / 3;

        for (int i = 0; i < 9; i++) {
            gridCells[i] = new Rectangle(gridX + ((i % 3) * cellSize), gridY + ((i / 3) * cellSize), cellSize, cellSize);
            spawnCells[i] = new Rectangle(spawnX + ((i % 3) * spawnCellSize), spawnY + ((i / 3) * spawnCellSize), spawnCellSize, spawnCellSize);
        }

        allNotes = loadMap("nevada.txt");

        // load music
        song = Gdx.audio.newMusic(Gdx.files.internal("nevada.mp3"));
        song.setVolume(0.5f);
        song.play();

        hitSound = Gdx.audio.newSound(Gdx.files.internal("hit.mp3"));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.01f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            if (song != null) { song.stop(); song.dispose(); }
            game.setScreen(new MenuScreen(game));
            return;
        }

        if (song != null && song.isPlaying()) songTimer = song.getPosition();

        handleMouseLocking();
        handleSweeperInput();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 1, 1, 1);
        drawThickRect(gridX, gridY, gridSize, gridSize, 3);

        for (Note n : allNotes) {
            float timeUntilHit = n.hitTime - songTimer;

            if (Math.abs(timeUntilHit) <= HIT_WINDOW) {
                n.isHittable = true;
            } else {
                n.isHittable = false;
            }

            if (timeUntilHit < -HIT_WINDOW && !n.hit) {
                n.passed = true;
            }

            if (!n.hit && !n.passed && timeUntilHit < APPROACH_TIME) {
                float rawProg = 1.0f - (timeUntilHit / APPROACH_TIME);
                float alpha = Math.min((APPROACH_TIME - timeUntilHit) / FADE_IN_DURATION, 1.0f);
                float easedProg = (float) Math.pow(rawProg * PUSHBACK, 3);

                float startCX = spawnCells[n.cell].x + spawnCells[n.cell].width / 2;
                float startCY = spawnCells[n.cell].y + spawnCells[n.cell].height / 2;
                float targetCX = gridCells[n.cell].x + gridCells[n.cell].width / 2;
                float targetCY = gridCells[n.cell].y + gridCells[n.cell].height / 2;

                float curX = startCX + (targetCX - startCX) * easedProg;
                float curY = startCY + (targetCY - startCY) * easedProg;
                float curSize = (gridCells[n.cell].width * 0.6f) * (SPAWN_SCALE + (1.0f - SPAWN_SCALE) * easedProg);

                if (n.isHittable) shapeRenderer.setColor(1, 1, 1, alpha);
                else shapeRenderer.setColor(0, 1, 1, alpha);

                drawThickRect(curX - curSize / 2, curY - curSize / 2, curSize, curSize, 5);
            }
        }
        shapeRenderer.end();

        game.batch.begin();
        game.font.draw(game.batch, "TIME: " + String.format("%.2f", songTimer), 50, 80);
        game.batch.end();
    }

    private void handleSweeperInput() {
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();

        for (Note n : allNotes) {
            if (n.isHittable && !n.hit && !n.passed) {
                if (gridCells[n.cell].contains(mx, my)) {
                    n.hit = true;

                    // hit sound when touch
                    if (hitSound != null) {
                        hitSound.play(1.0f); // Volume 100%
                    }

                    System.out.println("hit cell " + n.cell);
                    break;
                }
            }
        }
    }

    private void handleMouseLocking() {
        int mx = Gdx.input.getX();
        int my = Gdx.input.getY();
        float screenGridTop = Gdx.graphics.getHeight() - (gridY + gridSize);
        float screenGridBottom = Gdx.graphics.getHeight() - gridY;
        int wx = mx, wy = my;
        boolean warp = false;
        if (mx < gridX) { wx = (int)gridX; warp = true; }
        if (mx > gridX + gridSize) { wx = (int)(gridX + gridSize); warp = true; }
        if (my < screenGridTop) { wy = (int)screenGridTop; warp = true; }
        if (my > screenGridBottom) { wy = (int)screenGridBottom; warp = true; }
        if (warp) Gdx.input.setCursorPosition(wx, wy);
    }

    private void drawThickRect(float x, float y, float w, float h, int thickness) {
        for(int i = 0; i < thickness; i++) {
            shapeRenderer.rect(x - (i * 0.5f), y - (i * 0.5f), w + i, h + i);
        }
    }

    private Array<Note> loadMap(String filename) {
        Array<Note> notes = new Array<>();
        try {
            String content = Gdx.files.internal(filename).readString();
            String[] lines = content.split("\\r?\\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                notes.add(new Note(Float.parseFloat(parts[0]), Integer.parseInt(parts[1])));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return notes;
    }

    @Override
    public void dispose() {
        if (song != null) song.dispose();
        if (hitSound != null) hitSound.dispose(); // reset song
        shapeRenderer.dispose();
    }
}
