package com.rhythia.game.screens;

public class SongEntry implements Comparable<SongEntry> {
    public String title;
    public String audioFile;
    public String mapFile;

    public SongEntry(String title, String audioFile, String mapFile) {
        this.title = title;
        this.audioFile = audioFile;
        this.mapFile = mapFile;
    }

    public SongEntry(String title) {
        this(title, title + ".mp3", title + ".txt");
    }
    public int compareTo(SongEntry other) {
        return this.title.compareTo(other.title);
    }

    public String toString() {
        return title;
    }

    public String getAudioFile() {
        return audioFile;
    }

    public String getMapFile() {
        return mapFile;
    }
}
