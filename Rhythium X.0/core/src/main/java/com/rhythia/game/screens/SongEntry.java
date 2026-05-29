package com.rhythia.game.screens;

import java.util.TreeSet;

public class SongEntry implements Comparable<SongEntry> {
    public String title;
    public String audioFile;
    public String mapFile;
    public Long length;
    public static TreeSet<String> bestScore;

    /**
     * Creates a new Songentry
     * @param title
     * @param audioFile
     * @param mapFile
     */
    public SongEntry(String title, String audioFile, String mapFile) {
        this.title = title;
        this.audioFile = audioFile;
        this.mapFile = mapFile;
    }

    /**
     * Creates a new SongEntry
     * @param title
     */
    public SongEntry(String title) {
        this(title, title + ".mp3", title + ".txt");
    }
    
    /**
     * Calls String class's compareTo on this.title and other.title
     * @param other - other SongEntry
     */
    public int compareTo(SongEntry other) {
        return this.title.compareTo(other.title);
    }

    /**
     * Returns song name
     * @return song name
     */
    public String toString() {
        return title;
    }

    /**
     * Returns audioFile name
     * @return audioFile
     */
    public String getAudioFile() {
        return audioFile;
    }

    /**
     * returns mapFile name
     * @return mapFile
     */
    public String getMapFile() {
        return mapFile;
    }
}
