package com.whatdo.androidapps.voicePDF.framework;

/**
 * OverlapTester holds functions to test overlaps between points, circles, rectangles.
 */
public class OverlapTester {
    /**
     * Tests if two rectangles overlap.
     * 
     * @param r1 - a Rectangle rectangle
     * @param r2 - a Rectangle rectangle
     * @return true if both rectangles overlap; false otherwise
     */
    public static boolean overlapRectangles(Rectangle r1, Rectangle r2) {
        if(r1.lowerLeft_x < r2.lowerLeft_x + r2.width &&
           r1.lowerLeft_x + r1.width > r2.lowerLeft_x &&
           r1.lowerLeft_y < r2.lowerLeft_y + r2.height &&
           r1.lowerLeft_y + r1.height > r2.lowerLeft_y)
            return true;
        else
            return false;
    }

    /**
     * Tests if a point is in a rectangle.
     * 
     * @param r - a Rectangle rectangle
     * @param x - a float representing the x-coordinate of a point
     * @param y - a float representing the y-coordinate of a point
     * @return true if the point is in the rectangle; false otherwise
     */
    public static boolean pointInRectangle(Rectangle r, float x, float y) {
        return r.lowerLeft_x <= x && r.lowerLeft_x + r.width >= x &&
               r.lowerLeft_y <= y && r.lowerLeft_y + r.height >= y;
    }
}