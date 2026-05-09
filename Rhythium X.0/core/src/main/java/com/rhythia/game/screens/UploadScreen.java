package com.rhythia.game.screens;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.rhythia.game.Main;

import java.io.File;
import javax.swing.SwingUtilities;
import java.nio.file.Files;

public class UploadScreen extends ScreenAdapter{
    
    private Main game;
    private ShapeRenderer shapeRenderer;
    private int gridSize = 600;
    private int gridX, gridY;
    private Rectangle uploadBtn;
    private MenuScreen menuScreen;
    
    public UploadScreen(Main game, MenuScreen menuScreen) {
        this.game = game;
        this.shapeRenderer = new ShapeRenderer();
        this.menuScreen = menuScreen;
    }

    @Override
    public void show() {
        // setup
        gridX = (Gdx.graphics.getWidth() - gridSize) / 2;
        gridY = (Gdx.graphics.getHeight() - gridSize) / 2;

        uploadBtn = new Rectangle(gridX + 200, gridY + 250, 200, 60);

        System.out.println(uploadBtn.getX() + "|" + uploadBtn.getY());
        
    }

    @Override
    public void render(float delta) {
        // clear screen
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.01f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            
            game.setScreen(new MenuScreen(game));
            return;
        }


        if (Gdx.input.justTouched()) {
            float x = Gdx.input.getX();
            float y = Gdx.graphics.getHeight() - Gdx.input.getY();
            System.out.println(x + "," + y);

            handleUpload(uploadBtn, x, y);
            
            

        }
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 1, 1, 1);
        for (int i = 0; i < 3; i++) {
            shapeRenderer.rect(gridX - (i * 0.5f), gridY - (i * 0.5f), gridSize + i, gridSize + i);
        }
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.5f, 0.2f, 0.6f, 1);
        shapeRenderer.rect(uploadBtn.x, uploadBtn.y, uploadBtn.width, uploadBtn.height);
        shapeRenderer.end();

        
        game.batch.begin();
        game.font.getData().setScale(0.3f);
        game.font.draw(game.batch, "Upload Song", uploadBtn.x + 30, uploadBtn.y + 38);
        game.batch.end();
        
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }

    private void handleFile(File selectedFile) {
        File assetsDir = new File(System.getProperty("user.dir")); //idk why user.dir is assets folder, just trial and error
        File projectRoot = assetsDir.getParentFile();
        
        
        System.out.println(projectRoot);
        System.out.println(assetsDir);

        if (!assetsDir.exists()) assetsDir.mkdirs();

        File dest = new File(assetsDir, selectedFile.getName());
        System.out.println("Copying to: " + dest.getAbsolutePath());
        try {
            Files.copy(selectedFile.toPath(), dest.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Copy succeeded");
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

         
        System.out.println("Project root: " + projectRoot.getAbsolutePath());

        String scriptPath = new File(projectRoot,
        "core/src/main/java/com/rhythia/game/BeatProcesser.py").getAbsolutePath();

        String filePathForScript = dest.getAbsolutePath();

        System.out.println("Script path: " + scriptPath);
        System.out.println("File path for script: " + filePathForScript);
        
        ///Users/warrensu/Programming/Personal/Rhythium-X.0/Rhythium X.0/assets/core/src/main/java/com/rhythia/game/BeatProcesser.py'
        ///Users/warrensu/Programming/Personal/Rhythium-X.0/Rhythium X.0/assets/core/src/main/java/com/rhythia/game/BeatProcesser.py
        ///Users/warrensu/Programming/Personal/Rhythium-X.0/Rhythium X.0/core/src/main/java/com/rhythia/game/BeatProcesser.py
        ProcessBuilder pb = new ProcessBuilder(
            findPython(),
            scriptPath,
            "process_beats",
            filePathForScript
        );
        pb.directory(projectRoot);
        pb.environment().put("PATH", "/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin");
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream())
            );
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("PYTHON: " + line);
            }
            int exitCode = process.waitFor();
            System.out.println("Python exited with code: " + exitCode);
            
            // after processing, add song to menu
            if (exitCode == 0) {
                String songName = selectedFile.getName();
                String audioFile = songName;
                String mapFile = songName.replaceAll("\\.mp3$", ".txt");
                SongEntry newSong = new SongEntry(songName.replaceAll("\\.mp3$", ""), audioFile, mapFile);
                menuScreen.addSong(newSong);
                System.out.println("Song added to menu: " + newSong.title);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    

    private void handleUpload(Rectangle uploadBtn, float x, float y) {
        if (!uploadBtn.contains(x, y)) return;

        SwingUtilities.invokeLater(() -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("MP3 Files", "mp3"));

            int result = chooser.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                System.out.println("uploaded the mp3 file");
                handleFile(file);
            }

            
        });
    }

    

    private String findPython() {
        File cwd = new File(System.getProperty("user.dir"));
        cwd = cwd.getParentFile();
        System.out.println("find python cwd: " + cwd);
        File venvPython = new File(cwd, ".venv/bin/python");
        System.out.println(venvPython);
        if (venvPython.exists()) return venvPython.getAbsolutePath();
        return "python3"; // fallback
    }

    

    
    
}
