package com.rhythia.game.screens;

public class Note {
    public float hitTime;
    public int cell;
    public boolean hit = false;
    public boolean isHittable = false; // is it currently in the timing window?
    public boolean passed = false;     // did the player miss it?

    public Note(float hitTime, int cell) {
        this.hitTime = hitTime;
        this.cell = cell;
    }
}
