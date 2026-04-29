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

    public UploadScreen(Main game) {
        this.game = game;
        this.shapeRenderer = new ShapeRenderer();
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
        File assetsDir = resolveAssetsDir();
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

        File projectRoot = assetsDir.getParentFile();
        System.out.println("Project root: " + projectRoot.getAbsolutePath());

        String scriptPath = new File(projectRoot,
            "core/src/main/java/com/rhythia/game/BeatProcesser.py").getAbsolutePath();

        String filePathForScript = dest.getAbsolutePath();
        String mapFileName = removeExtension(selectedFile.getName()) + ".txt";

        System.out.println("Script path: " + scriptPath);
        System.out.println("File path for script: " + filePathForScript);

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
            if (exitCode != 0) {
                System.out.println("Python processing failed with exit code: " + exitCode);
                return;
            }
            File generatedMap = new File(assetsDir, mapFileName);
            if (!generatedMap.exists()) {
                System.out.println("Generated map file not found: " + generatedMap.getAbsolutePath());
                return;
            }
            Gdx.app.postRunnable(() -> game.setScreen(new GameplayScreen(game, selectedFile.getName(), mapFileName)));
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

    private File resolveAssetsDir() {
        File cwd = new File(System.getProperty("user.dir"));
        if ("assets".equals(cwd.getName())) {
            return cwd;
        }
        return new File(cwd, "assets");
    }

    private String removeExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot <= 0) return filename;
        return filename.substring(0, dot);
    }
    private String findPython() {
        String[] candidates = {
            "/opt/homebrew/bin/python3",   // Mac Apple Silicon (Homebrew)
            "/usr/local/bin/python3",      // Mac Intel (Homebrew)
            "/opt/homebrew/bin/python",    // some Homebrew setups
            "/usr/bin/python3",            // Linux / Mac system
            "/usr/local/bin/python",
            "python3",                     // fallback
            "python"
        };
        for (String candidate : candidates) {
            File f = new File(candidate);
            if (f.exists()) return candidate;
        }
        return "python3";
    }


    

    
    
}
