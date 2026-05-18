package com.rhythia.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound; // New Import
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.rhythia.game.Main;

public class GameplayScreen extends ScreenAdapter {
    private final Main game;
    private Music song;
    private SongEntry entry;
    private Sound hitSound; // sound variable
    private float songTimer = 0.0f;
    private ShapeRenderer shapeRenderer;
    private double hit = 0;
    private Long duration;

    private Rectangle[] gridCells = new Rectangle[9];
    private Rectangle[] spawnCells = new Rectangle[9];
    private Array<Note> allNotes = new Array<>();
    private float[] allColors;
    private float gridSize = 600;
    private float gridX, gridY;
    private Vector3 touchPoint;

    // changeable settings
    private final float APPROACH_TIME = 1.4f;
    private final float FADE_IN_DURATION = 0.5f;
    private final float PUSHBACK = 0.94f;
    private final float SPAWN_SCALE = 0.10f;
    private float HIT_WINDOW = 0.06f; // original: 0.12f

    /**
     * Constructs a GameplayScreen
     * @param game - initializes game
     */
    public GameplayScreen(Main game, SongEntry entry, float[] theme, String difficulty) {
        this.game = game;
        this.entry = entry; 
        this.shapeRenderer = new ShapeRenderer();
        this.duration = entry.length;
        this.allColors = theme;
        this.touchPoint = new Vector3();
        if (difficulty.equalsIgnoreCase("Easy")) HIT_WINDOW = 0.06f;
        else if (difficulty.equalsIgnoreCase("Medium")) HIT_WINDOW = 0.03f;
        else if (difficulty.equalsIgnoreCase("Hard")) HIT_WINDOW = 0.01f;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
        Gdx.input.setCursorCatched(false);

        // Gdx.gl.glEnable(GL20.GL_BLEND);
        // Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        //0 1 2
        //3 4 5
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

        allNotes = loadMap(entry.getMapFile());

        // load music
        song = Gdx.audio.newMusic(Gdx.files.internal(entry.getAudioFile()));
        song.setVolume(0.5f);
        song.play();

        hitSound = Gdx.audio.newSound(Gdx.files.internal("hit.mp3"));
    }

    @Override
    public void render(float delta) {
        // clear screen
        //Gdx.gl.glClearColor(0.01f, 0.01f, 0.01f, 1);
        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.solidBackground = new Texture(Gdx.files.internal("background1.png"));
        game.batch.draw(
            game.solidBackground,
            0, 
            0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
       );

       if (!song.isPlaying()) {
            Rectangle rec = new Rectangle(895.375f, 500, 200, 60); //958.375, 540
            game.font.draw(game.batch, "Return", 895.375f, 560);
            // GlyphLayout layout = new GlyphLayout();
            // layout.setText(game.font, "Return");
            // System.out.println(958.375 - layout.width/2);
            // System.out.println(540-layout.height/2);

            if (Gdx.input.justTouched()) {
                // System.out.println(Gdx.input.getX() + ", " + Gdx.input.getY());
                touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                float flippedY = Gdx.graphics.getHeight() - touchPoint.y;
                if (rec.contains(touchPoint.x, flippedY)) {
                    if (song != null) { song.stop(); song.dispose(); }
                    game.batch.end();
                    game.setScreen(new MenuScreen(game));
                    return;
                }
            }
        }
        game.batch.end();

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
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GRAY);
        shapeRenderer.rect(gridX + 50, gridY + gridSize + 25, gridSize - 100, 15);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(gridX + 50, gridY + gridSize + 25, (gridSize - 100) * songTimer / duration, 15);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // update, process, draw notes
        int missedNotes = 0, i = 0; // notes not hit yet

        // calculating the HP, streak, points
        float HP = 6;
        int points = 0, streak = 0;

        boolean a = true;
        Note n1 = null; Note n2 = null; 
        int addedPoints = 0;
        for (int k = 0; k < allNotes.size; k++) {
            Note n = allNotes.get(k);
            float timeUntilHit = n.hitTime - songTimer;
            
            // finding n1, n2
            if (timeUntilHit > 0 && a) {
                a = false;
                n2 = n; 
                if (k-1 >= 0) n1 = allNotes.get(k-1);
            }
            
            if (Math.abs(timeUntilHit) <= HIT_WINDOW) {
                n.isHittable = true;
            } else {
                n.isHittable = false;
            }

            if (timeUntilHit < -HIT_WINDOW && !n.hit) {
                n.passed = true;
                missedNotes++;
                displayJudgement("MISS", songTimer - n.hitTime, 0, n);
            }
            else if (timeUntilHit < -HIT_WINDOW && n.hit) {
                n.passed = true;
                float judgement = 0.5f;
                if (n.rating.equalsIgnoreCase("PERFECT!")) judgement = 1.0f;
                else if (n.rating.equalsIgnoreCase("GREAT!")) judgement = 0.7f;
                addedPoints = (int) (300 * judgement * (1 + streak * 0.1));
                points += addedPoints;
                displayJudgement(n.rating, songTimer - n.hitTime, addedPoints, n);
            }

            // shows all unpassed notes
            if (!n.hit && !n.passed) {i++;}
            if (n.passed) {
                if (n.hit) {
                    if (HP < 6) HP += 0.5;
                    streak++;
                }
                else if (!n.hit) {
                    HP--;
                    streak = 0;
                }
            }
        }

        // draw grid
        Color color = new Color(Color.WHITE);
        if (n1 != null && n2 != null) color = updateGridColor(songTimer, n1, n2);
        shapeRenderer.setColor(color);
        drawThickRect(gridX, gridY, gridSize, gridSize, 5);
        // shapeRenderer.setColor(Color.WHITE);

        for (int j = allNotes.size - 1; j >= 0; j--) {
            // Note: forwardN runs a forward loop to calculate HP, n renders the cubes to maintain layering
            Note n = allNotes.get(j);
            float timeUntilHit = n.hitTime - songTimer;

            // hit sound when touch
            if (hitSound != null && n.hit && timeUntilHit <= 0.00000001 && !n.playedSound) {
                hitSound.play(1.0f); // Volume 100%
                // System.out.println("Played sound!");
                n.playedSound = true;
            }
            // shows approaching notes within approach interval
            /* TODO if you want to increase acceleration, cut approach time, speed stuff up */
            if (!n.hit && !n.passed && timeUntilHit < APPROACH_TIME) {
                float rawProg = 1.0f - (timeUntilHit / APPROACH_TIME);
                // float alpha = Math.min((APPROACH_TIME - timeUntilHit) / FADE_IN_DURATION, 1.0f);
                float easedProg = (float) Math.pow(rawProg * 1.001, 3); //original: rawProg * PUSHBACK

                float startCX = spawnCells[n.cell].x + spawnCells[n.cell].width / 2;
                float startCY = spawnCells[n.cell].y + spawnCells[n.cell].height / 2;
                float targetCX = gridCells[n.cell].x + gridCells[n.cell].width / 2;
                float targetCY = gridCells[n.cell].y + gridCells[n.cell].height / 2;

                float curX = startCX + (targetCX - startCX) * easedProg;
                float curY = startCY + (targetCY - startCY) * easedProg;
                float curSize = (gridCells[n.cell].width * 0.6f) * (SPAWN_SCALE + (1.0f - SPAWN_SCALE) * easedProg);

                // making pulses, setting color
                float t = songTimer;
                if (j - 1 >= 0) t -= allNotes.get(j - 1).hitTime; // edit of time gap bettween 2 notes is too long
                float v = 0.85f + 0.15f * (float) Math.sin(t * 4f + Math.PI/2); // original 0.95 + 0.05x
                float al = 0.4f + 0.6f * (float) Math.abs(Math.sin(t * 4f + Math.PI/2));
                Color c = new Color();
                c.fromHsv(n.color, n.saturation, v);
                c.a = al;
                shapeRenderer.setColor(c);

                // mimicking 3D, the rectangle grows larger as it approaches approachtime
                int thickness = (int) (curSize / 6); //timeuntilhit is default < 1
                //
                drawThickRect(curX - curSize / 2, curY - curSize / 2, curSize, curSize, thickness);
            }
        }
        shapeRenderer.end();
        
        // if (HP <= 0) { //TODO uncomment when game is finished, this is the 0-hp consequence
        // game.setScreen(new MenuScreen(game));
        // return;
        // }
        displayStats(i, missedNotes, streak, HP, points, color);
    }

    private Color updateGridColor(float deltaTime, Note n1, Note n2)
    {
        float interTime = n2.hitTime - n1.hitTime;
        Color n1C = new Color().fromHsv(n1.color, n1.saturation, 0.9f);
        Color n2C = new Color().fromHsv(n2.color, n2.saturation, 0.9f);
        float deltaTR = (n2C.r - n1C.r)/interTime;
        float deltaTG = (n2C.g - n1C.g)/interTime;
        float deltaTB = (n2C.b - n1C.b)/interTime;
        float deltaCR = (songTimer - n1.hitTime) * deltaTR;
        float deltaCG = (songTimer - n1.hitTime) * deltaTG;
        float deltaCB = (songTimer - n1.hitTime) * deltaTB;
        return new Color(n1C.r + deltaCR, n1C.g + deltaCG, n1C.b + deltaCB, 0.9f);
    }

    /**
     * Displays statistics: Misses, HP, Notes, Accuracy, Streak, Time, Title
     */
    private void displayStats(int i, int missedNotes, int streak, double HP, int points, Color c)
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
        String sec2 = duration % 60 + "";
        if ((int) (duration % 60) / 10 < 1) sec2 = "0" + (int) (duration % 60);
        String time = min + ":" + sec1 + " / " + duration / 60 + ":" + sec2;
        // System.out.println(); 

        GlyphLayout layout = new GlyphLayout();

        game.batch.begin();

        // game.font.setColor(c);

        game.font.getData().setScale(0.35f);
        float accC = displayStatsHelper("ACCURACY", a, 450, 390, layout);
        float missC = displayStatsHelper("MISSES", missedNotes + "", 1320, 720, layout);
        layout.setText(game.font, "HP");
        displayStatsHelper("HP", HP + "", missC - layout.width/2f, 560, layout);
        layout.setText(game.font, "NOTES");
        displayStatsHelper("NOTES", notes, missC - layout.width/2f, 390, layout);
        float xCenter = (gridX + (gridX + gridSize))/2f;
        layout.setText(game.font, entry.title);
        displayStatsHelper(entry.title, time, xCenter - layout.width/2f, 970, layout);
        // System.out.println(xCenter);
        layout.setText(game.font, "POINTS");
        displayStatsHelper("POINTS", points + "", accC - layout.width/2f, 560, layout);

        game.font.getData().setScale(0.5f);
        layout.setText(game.font, streak + "x");
        game.font.draw(game.batch, streak + "x", accC - layout.width/2f, 710);

        game.font.setColor(Color.WHITE);
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
        game.font.getData().setScale(0.35f);
        return center;
    }

    private void displayJudgement(String judgement, float timeSinceHit, double points, Note n)
    {
        // NOTE: for setColor(), divide all r,g,b values by 255f for wanted result, or will result in white color
        float alpha = 1 - timeSinceHit / 0.8f; // stuff disappears when alpha > 0.8f
        if (alpha <= 0) return;
        float yOffset = timeSinceHit * 20f;
        float scale = 1 + timeSinceHit * 0.1f;
        game.batch.begin();
        switch (judgement) { //decided attributes for each case
            case "PERFECT!":
                game.font.setColor(255/255f, 245/255f, 160/255f, alpha); // 255, 245, 160
                game.font.getData().setScale(0.44f);
                yOffset = 90;
                break;
            case "GREAT!":
                game.font.setColor(120/255f, 220/255f, 255/255f, alpha); // 120, 220, 255
                game.font.getData().setScale(0.4f);
                yOffset = 80;
                break;
            case "GOOD!":
                game.font.setColor(140/255f, 255/255f, 140/255f, alpha); // 140, 255, 140
                game.font.getData().setScale(0.37f);
                yOffset = 70;
                break;
            default: //miss
                game.font.setColor(255/255f, 100/255f, 100/255f, alpha); // 255, 100, 100
                game.font.getData().setScale(0.33f);
                yOffset = 60;
        }
        // draw the image, center = 
        GlyphLayout layout = new GlyphLayout();
        layout.setText(game.font, judgement);
        float x = gridCells[n.cell].x + gridCells[n.cell].width / 2;
        float y = gridCells[n.cell].y + gridCells[n.cell].height / 2;
        game.font.draw(game.batch, judgement, 958.375f - layout.width/2, 540 + yOffset); // center ~ 958.375, 540
        game.font.getData().setScale(0.20f);
        game.font.draw(game.batch, "+" + points, x, y); // center ~ 958.375, 540
        // change colors + size + whatnot back to normal
        game.font.setColor(Color.WHITE);
        game.font.getData().setScale(1);
        game.batch.end();
    }

    private void handleSweeperInput() {
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();

        for (Note n : allNotes) {
            if (n.isHittable && !n.hit && !n.passed) {
                if (gridCells[n.cell].contains(mx, my)) {
                    n.hit = true;

                    // if within certain range --> perfect, or blah blah
                    float timeUntilHit = n.hitTime - songTimer;
                    if (timeUntilHit < HIT_WINDOW * 0.1) n.rating = "PERFECT!";
                    else if (timeUntilHit < HIT_WINDOW * 0.5) n.rating = "GREAT!";
                    else n.rating = "GOOD!";

                    // hit sound when touch
                    // if (hitSound != null) {
                    //     hitSound.play(1.0f); // Volume 100%
                    // }

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
        for(int i = 0; i < thickness; i++) {
            shapeRenderer.rect(x - (i * 0.5f), y - (i * 0.5f), w + i, h + i);
        }
        // float r = Math.min(Math.min(w, h) * 0.5f, 200f);
        // shapeRenderer.setColor(new Color(0,0,0,0));
        // drawRoundedRect(x, y, w, h, w * 0.15f); // inner

        // shapeRenderer.setColor(c);
        // float a = (thickness-1) * 0.5f;
        // drawRoundedRect(x - a, y - a, w + a*2, h + a*2, w * 0.15f + a); // outer
    }

    private void drawRoundedRect(float x, float y, float w, float h, float r) {
        r = Math.min(r, Math.min(w,h) * 0.5f - 1);
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
