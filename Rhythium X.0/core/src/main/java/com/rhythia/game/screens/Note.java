package com.rhythia.game.screens;

import com.badlogic.gdx.graphics.Color;

public class Note {
    public float hitTime;
    public int cell;
    public boolean hit = false;
    public boolean isHittable = false; // is it currently in the timing window?
    public boolean passed = false;     // did the player miss it?
    public float color; // random color
    public float saturation;
    public float offset = (float) Math.random() * 10;

    public Note(float hitTime, int cell, float color, float s) {
        this.hitTime = hitTime;
        this.cell = cell;
        this.color = color;
        saturation = s;
    }
}
