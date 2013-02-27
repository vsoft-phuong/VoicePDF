package com.whatdo.androidapps.voicePDF.framework;

/**
 * A rectangle represented by its lower-left corner position and its width and height.
 */
public class Rectangle {
    public final float lowerLeft_x, lowerLeft_y;
    public float width, height;
    
    public Rectangle(float x, float y, float width, float height) {
        this.lowerLeft_x = x;
        this.lowerLeft_y = y;
        this.width = width;
        this.height = height;
    }
}
