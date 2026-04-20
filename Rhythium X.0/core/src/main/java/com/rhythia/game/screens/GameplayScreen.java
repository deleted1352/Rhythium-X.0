package com.rhythia.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound; // New Import
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.rhythia.game.Main;

public class GameplayScreen extends ScreenAdapter {
    private final Main game;
    private Music song;
    private Sound hitSound; // sound variable
    private float songTimer = 0.0f;
    private ShapeRenderer shapeRenderer;
    private double hit = 0;

    private Rectangle[] gridCells = new Rectangle[9];
    private Rectangle[] spawnCells = new Rectangle[9];
    private Array<Note> allNotes = new Array<>();
    private float[] allColors = {325, 0.15f, 296, 0.50f, 273, 0.84f, 273, 1.00f, 241, 0.98f, 180, 1, 145, 0.60f, 55, 0.80f, 48, 0.80f};
    // colors, respectively: pale pink, neon pink, purple, deep violet, deep blue, cyan, mint green, neon yellow, neon yellow 2
    private float gridSize = 600;
    private float gridX, gridY;

    // changeable settings
    private final float APPROACH_TIME = 1.4f;
    private final float FADE_IN_DURATION = 0.5f;
    private final float PUSHBACK = 0.94f;
    private final float SPAWN_SCALE = 0.10f;
    private final float HIT_WINDOW = 0.12f; // original: 0.12f

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
        // clear screen
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.01f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // handle escape
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            if (song != null) { song.stop(); song.dispose(); }
            game.setScreen(new MenuScreen(game));
            return;
        }

        // get songtimer
        if (song != null && song.isPlaying()) songTimer = song.getPosition();

        handleMouseLocking();
        handleSweeperInput();

        // grid drawing
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 1, 1, 1);
        drawThickRect(gridX, gridY, gridSize, gridSize, 3);

        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // update, process, draw notes
        int missedNotes = 0, i = 0, streak = 0; // notes not hit yet
        boolean s = true;
        double HP = 6;
        for (int j = allNotes.size - 1; j >= 0; j--) {
            Note n = allNotes.get(j);
            float timeUntilHit = n.hitTime - songTimer;

            if (Math.abs(timeUntilHit) <= HIT_WINDOW) {
                n.isHittable = true;
            } else {
                n.isHittable = false;
            }

            if (timeUntilHit < -HIT_WINDOW && !n.hit) {
                n.passed = true;
                missedNotes++;
                HP--;
                s = false;
            }
            else if (timeUntilHit < -HIT_WINDOW && n.hit) {
                if (HP < 6) HP += 0.5;
                if (s) streak++;
            }

            // shows all unpassed notes
            if (!n.hit && !n.passed) {i++;}

            // shows approaching notes within approach interval
            if (!n.hit && !n.passed && timeUntilHit < APPROACH_TIME) {
                float rawProg = 1.0f - (timeUntilHit / APPROACH_TIME);
                // float alpha = Math.min((APPROACH_TIME - timeUntilHit) / FADE_IN_DURATION, 1.0f);
                float easedProg = (float) Math.pow(rawProg * PUSHBACK, 3);

                float startCX = spawnCells[n.cell].x + spawnCells[n.cell].width / 2;
                float startCY = spawnCells[n.cell].y + spawnCells[n.cell].height / 2;
                float targetCX = gridCells[n.cell].x + gridCells[n.cell].width / 2;
                float targetCY = gridCells[n.cell].y + gridCells[n.cell].height / 2;

                float curX = startCX + (targetCX - startCX) * easedProg;
                float curY = startCY + (targetCY - startCY) * easedProg;
                float curSize = (gridCells[n.cell].width * 0.6f) * (SPAWN_SCALE + (1.0f - SPAWN_SCALE) * easedProg);

                // making pulses, setting color
                float t = songTimer + n.offset;
                float v = 0.95f + 0.05f * (float) Math.sin(t * 4f);
                Color c = new Color();
                c.fromHsv(n.color, n.saturation, v);
                shapeRenderer.setColor(c);

                // mimicking 3D, the rectangle grows larger as it approaches approachtime
                int thickness = (int) (curSize/2); //timeuntilhit is default < 1
                drawThickRoundedRect(curX - curSize / 2, curY - curSize / 2, curSize, curSize, thickness);                
            }
        }
        shapeRenderer.end();
        // if (HP <= 0) game.batch.end(); // CHANGE TO END SCREEN, THIS CLOSES WHOLE APP
        displayStats(i, missedNotes, streak, HP);
        
    }

    private void displayStats(int i, int missedNotes, int streak, double HP)
    {
        // displayingHelper: Misses, HP, Notes, and Accuracy
        // Self-display: 9x, Title, Time

        /*
        Accuracy: 450, 520, %: center - width/2, 460
        Misses: 1320, 720, m: mcenter - width/2, 660
        HP: mcenter - width/2, 560, hp: mcenter - width/2, 500
        Notes: mcenter - width/2, 390, n: 330
        */
        double current = allNotes.size - i;
        hit = current - missedNotes;
        String a = String.format("%.2f", hit / current * 100) + "%";
        String notes = (int) hit + "/" + (int) current;
        
        int sec = (int) songTimer % 60;
        int min = (int) songTimer / 60;
        String sec1 = sec + "";
        if (sec / 10 < 1) sec1 = "0" + sec;
        String time = min + ":" + sec1 + " / 3:28";

        GlyphLayout layout = new GlyphLayout();

        game.batch.begin();

        float accC = displayStatsHelper("ACCURACY", a, 450, 520, layout);
        float missC = displayStatsHelper("MISSES", missedNotes + "", 1320, 720, layout);
        layout.setText(game.font, "HP");
        displayStatsHelper("HP", HP + "", missC - layout.width/2, 560, layout);
        layout.setText(game.font, "NOTES");
        displayStatsHelper("NOTES", notes, missC - layout.width/2, 390, layout);
        displayStatsHelper("ViceTone - Nevada", time, 835, 940, layout);

        game.font.getData().setScale(0.5f);
        layout.setText(game.font, streak + "x");
        game.font.draw(game.batch, streak + "x", accC - layout.width/2, 710);
        game.batch.end();
    }

    private float displayStatsHelper(String message, String stat, float x, float y, GlyphLayout layout)
    {
        // msg = accuracy, stat = a
        game.font.getData().setScale(0.35f);
        layout.setText(game.font, message);
        float center = x + layout.width/2f;
        game.font.draw(game.batch, message, x, y);

        game.font.getData().setScale(0.25f);
        layout.setText(game.font, stat);
        game.font.draw(game.batch, stat, center - layout.width/2f, y - 60f);
        return center;
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

                    // System.out.println("hit cell " + n.cell);
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

    private void drawThickRoundedRect(float x, float y, float w, float h, int thickness) {
        float a = (thickness-1) * 0.5f;
        drawRoundedRect(x - a, y - a, w + a*2, h + a*2, w * 0.15f + a); // outer
        shapeRenderer.setColor(Color.BLACK);
        drawRoundedRect(x, y, w, h, w * 0.15f); // inner
}

    private void drawRoundedRect(float x, float y, float w, float h, float r) {
        // center rects
        shapeRenderer.rect(x + r, y, w - 2*r, h);
        shapeRenderer.rect(x, y + r, w, h - 2*r);

        // quarter-circle corners
        shapeRenderer.arc(x + r, y + r, r, 180, 90);
        shapeRenderer.arc(x + w - r, y + r, r, 270, 90);
        shapeRenderer.arc(x + r, y + h - r, r,  90, 90);
        shapeRenderer.arc(x + w - r, y + h - r, r,   0, 90);
    }


    private Array<Note> loadMap(String filename) {
        Array<Note> notes = new Array<>();
        try {
            String content = Gdx.files.internal(filename).readString();
            String[] lines = content.split("\\r?\\n");
            int i = 0, j = 1;
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                float c = allColors[i % allColors.length];
                float s = allColors[j % allColors.length];
                notes.add(new Note(Float.parseFloat(parts[0]), Integer.parseInt(parts[1]), c, s));
                i += 2; j += 2;
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
