package com.dominicmortlock.littelifesim;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Utility class for common rendering operations.
 */
public class RenderUtils {
    
    /**
     * Renders a rotated rectangle using triangles.
     * 
     * @param shapeRenderer The shape renderer to use
     * @param x Rectangle x position
     * @param y Rectangle y position  
     * @param width Rectangle width
     * @param height Rectangle height
     * @param rotation Rotation angle in radians
     * @param pivotX Pivot point x coordinate
     * @param pivotY Pivot point y coordinate
     */
    public static void renderRotatedRectangle(ShapeRenderer shapeRenderer, 
                                            float x, float y, float width, float height,
                                            float rotation, float pivotX, float pivotY) {
        
        // If no rotation, draw normally
        if (Math.abs(rotation) < 0.01f) {
            shapeRenderer.rect(x, y, width, height);
            return;
        }
        
        // Calculate the four corners of the rotated rectangle
        float cos = MathUtils.cos(rotation);
        float sin = MathUtils.sin(rotation);
        
        float halfWidth = width / 2;
        float halfHeight = height / 2;
        
        // Corner offsets from rectangle center
        float[] corners = {
            -halfWidth, -halfHeight, // Bottom-left
            halfWidth, -halfHeight,  // Bottom-right
            halfWidth, halfHeight,   // Top-right
            -halfWidth, halfHeight   // Top-left
        };
        
        // Rectangle center relative to pivot point
        float rectCenterX = x + width / 2;
        float rectCenterY = y + height / 2;
        float offsetX = rectCenterX - pivotX;
        float offsetY = rectCenterY - pivotY;
        
        // Rotate and translate corners around pivot point
        float[] rotatedCorners = new float[8];
        for (int i = 0; i < 4; i++) {
            float localX = corners[i * 2] + offsetX;
            float localY = corners[i * 2 + 1] + offsetY;
            
            rotatedCorners[i * 2] = pivotX + (localX * cos - localY * sin);
            rotatedCorners[i * 2 + 1] = pivotY + (localX * sin + localY * cos);
        }
        
        // Draw as a triangle fan (two triangles)
        shapeRenderer.triangle(
            rotatedCorners[0], rotatedCorners[1], // Bottom-left
            rotatedCorners[2], rotatedCorners[3], // Bottom-right
            rotatedCorners[4], rotatedCorners[5]  // Top-right
        );
        shapeRenderer.triangle(
            rotatedCorners[0], rotatedCorners[1], // Bottom-left
            rotatedCorners[4], rotatedCorners[5], // Top-right
            rotatedCorners[6], rotatedCorners[7]  // Top-left
        );
    }
}
