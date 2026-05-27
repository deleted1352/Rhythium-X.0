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
    
    private volatile boolean openChooser = false;
    private volatile boolean choosingFile = false;
    public UploadScreen(Main game, MenuScreen menuScreen) {
        this.game = game;
        this.shapeRenderer = new ShapeRenderer();
        this.menuScreen = menuScreen;
    }

    @Override
    /**
     * Initializes the grid dimensions and the upload button.
     *
     * gridX and gridY read from the user's window, making the grid responsive.
     * The upload button is initialized as a rectangle. This allows us to later 
     * read it's position and use built in collision functionality to register clicks 
     *
     * 
     */
    public void show() {
        // setup
        gridX = (Gdx.graphics.getWidth() - gridSize) / 2;
        gridY = (Gdx.graphics.getHeight() - gridSize) / 2;

        uploadBtn = new Rectangle(gridX + 200, gridY + 250, 200, 60);

        System.out.println(uploadBtn.getX() + "|" + uploadBtn.getY());
        
    }

    @Override
    /**
     * Periodically renders the screen, where clicks are registered.
     * 
     * A rectangle is drawn, and then click is checked to be on upload button using collision logic.
     * If the click was for the upload button, a new window is presented.
     * The escape key is also handled for exiting back to menu screen.
     *
     * @param delta  The time in seconds since last render
     */
    public void render(float delta) {
        // clear screen
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.01f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            
            game.setScreen(menuScreen);
            return;
        }

        if (Gdx.input.justTouched() && !choosingFile) {
            //float x = Gdx.input.getX();
            //float y = Gdx.graphics.getHeight() - Gdx.input.getY();
            //System.out.println(x + "," + y);

            //handleUpload(uploadBtn, x, y);
            float x = Gdx.input.getX();
            float y = Gdx.graphics.getHeight() - Gdx.input.getY();
            System.out.println(x + "," + y);

            // Only open file chooser if clicked on button
            if (uploadBtn.contains(x, y)) {
                handleUpload();
            }

            

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

    /**
     * Accepts a file, and then calls a python script to get a working SongEntry. 
     * 
     * Upload the selected file to assets and read its path. Then, use a ProcessBuilder to execute a 
     * python script that processes this mp3 file to generate the txt level file. The version of Python
     * used is found with a helper function. Then, create a SongEntry based on the txt file. 
     * Add this SongEntry to the MenuScreen's song list
     *
     * @param selectedFile the file to be passed into the python script
     */
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
                System.out.println(menuScreen.getSongs());
                //game.setScreen(menuScreen);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Opens a file chooser where users upload an MP3 file.
     * 
     * 
     * On file selection, spawns a new thread to process the file without blocking the render thread.
     * 
     */
    private void handleUpload() {

        choosingFile = true;

        Gdx.graphics.setContinuousRendering(true);

        SwingUtilities.invokeLater(() -> {

            JFileChooser chooser = new JFileChooser();

            chooser.setFileFilter(
                new FileNameExtensionFilter("MP3 Files", "mp3")
            );

            int result = chooser.showOpenDialog(null);

            choosingFile = false;

            Gdx.graphics.setContinuousRendering(true);

            Gdx.graphics.requestRendering();

            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File file = chooser.getSelectedFile();

            new Thread(() -> {

                handleFile(file);

                Gdx.app.postRunnable(() -> {

                    game.setScreen(menuScreen);

                });

            }).start();
        });
    }
    
    /**
     * Find a working version of python.
     * 
     * If the user followed the directions, the path to the virtual environment is used.
     *
     * @return The path to a working version of python
     */
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
