package com.dominicmortlock.littelifesim;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Displays text above entities using simple pixel art characters.
 * Supports mood-based text with probability and duration controls.
 */
public class TextDisplay {
    private String text;
    private float x, y;
    private float lifetime;
    private float maxLifetime;
    private boolean active;
    private Entity owner;
    private String currentMood;
    private Random random;
    private static Map<Character, float[][]> characterPatterns;
    
    // Visual properties
    private static final float OFFSET_X = 5f;
    private static final float OFFSET_Y = 15f;
    private static final float CHAR_WIDTH = 12f;
    private static final float CHAR_HEIGHT = 16f;
    private static final float BOLD_THICKNESS = 2f;
    
    static {
        initializeCharacterPatterns();
    }
    
    public TextDisplay(Entity owner) {
        this.owner = owner;
        this.text = "";
        this.lifetime = 0f;
        this.maxLifetime = 2f;
        this.active = false;
        this.currentMood = "";
        this.random = new Random();
    }
    
    public void setMood(String mood, float likelihood, float duration) {
        this.currentMood = mood;
        
        // Show thought based on likelihood
        if (random.nextFloat() < likelihood) {
            show(mood, duration);
        }
    }
    
    public void show(String text, float duration) {
        this.text = text;
        this.maxLifetime = duration;
        this.lifetime = 0f;
        this.active = true;
        updatePosition();
    }
    
    public void show(String text) {
        show(text, 2f);
    }
    
    public void update(float deltaTime) {
        if (!active) return;
        
        lifetime += deltaTime;
        if (lifetime >= maxLifetime) {
            active = false;
        }
        
        updatePosition();
    }
    
    private void updatePosition() {
        if (owner != null) {
            this.x = owner.getX() + owner.getWidth() / 2f + OFFSET_X;
            this.y = owner.getY() + owner.getHeight() + OFFSET_Y;
        }
    }
    
    public void render(ShapeRenderer shapeRenderer) {
        if (!active || text.isEmpty()) return;
        
        // Calculate fade effect
        float alpha = 1f;
        if (lifetime > maxLifetime * 0.7f) {
            float fadeProgress = (lifetime - maxLifetime * 0.7f) / (maxLifetime * 0.3f);
            alpha = 1f - fadeProgress;
        }
        
        // Draw text as simple pixel rectangles
        drawText(shapeRenderer, text, x, y, alpha);
    }
    
    private void drawText(ShapeRenderer shapeRenderer, String text, float startX, float startY, float alpha) {
        float currentX = startX;
        
        // First pass: Draw white background/outline for each character
        shapeRenderer.setColor(1f, 1f, 1f, alpha * 0.9f); // White background
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // Draw background rectangle for each character
            shapeRenderer.rect(currentX - 1, startY - 1, CHAR_WIDTH + 2, CHAR_HEIGHT + 2);
            currentX += CHAR_WIDTH + 1f;
        }
        
        // Second pass: Draw black text on top
        shapeRenderer.setColor(0f, 0f, 0f, alpha); // Black text
        currentX = startX;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            drawCharacter(shapeRenderer, c, currentX, startY);
            currentX += CHAR_WIDTH + 1f; // Character width + spacing
        }
    }
    
    private static void initializeCharacterPatterns() {
        characterPatterns = new HashMap<>();
        
        // Each pattern is an array of rectangles: [x, y, width, height]
        characterPatterns.put('.', new float[][]{{2, 0, BOLD_THICKNESS, BOLD_THICKNESS}});
        characterPatterns.put('!', new float[][]{{2, 2, BOLD_THICKNESS, 4}, {2, 0, BOLD_THICKNESS, BOLD_THICKNESS}});
        characterPatterns.put('?', new float[][]{{0, 5, 4, BOLD_THICKNESS}, {3, 3, BOLD_THICKNESS, BOLD_THICKNESS}, 
                                                {2, 2, BOLD_THICKNESS, BOLD_THICKNESS}, {2, 0, BOLD_THICKNESS, BOLD_THICKNESS}});
        characterPatterns.put('z', new float[][]{{0, 3, 4, BOLD_THICKNESS}, {3, 2, BOLD_THICKNESS, BOLD_THICKNESS}, 
                                                {1, 1, BOLD_THICKNESS, BOLD_THICKNESS}, {0, 0, 4, BOLD_THICKNESS}});
        characterPatterns.put('Z', new float[][]{{0, 5, 4, BOLD_THICKNESS}, {3, 4, BOLD_THICKNESS, BOLD_THICKNESS}, 
                                                {2, 3, BOLD_THICKNESS, BOLD_THICKNESS}, {1, 2, BOLD_THICKNESS, BOLD_THICKNESS}, 
                                                {0, 0, 4, BOLD_THICKNESS}});
        characterPatterns.put('h', new float[][]{{0, 0, BOLD_THICKNESS, 6}, {1, 3, BOLD_THICKNESS, BOLD_THICKNESS}, 
                                                {3, 0, BOLD_THICKNESS, 4}});
        characterPatterns.put('m', new float[][]{{0, 0, BOLD_THICKNESS, 4}, {1, 3, BOLD_THICKNESS, BOLD_THICKNESS}, 
                                                {3, 0, BOLD_THICKNESS, 4}, {4, 3, BOLD_THICKNESS, BOLD_THICKNESS}, 
                                                {6, 0, BOLD_THICKNESS, 4}});
    }
    
    private void drawCharacter(ShapeRenderer shapeRenderer, char c, float x, float y) {
        float[][] pattern = characterPatterns.get(c);
        if (pattern != null) {
            for (float[] rect : pattern) {
                shapeRenderer.rect(x + rect[0], y + rect[1], rect[2], rect[3]);
            }
        } else {
            // Default rectangle for unknown characters
            shapeRenderer.rect(x, y, 3, 4);
        }
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void hide() {
        active = false;
    }
    
    public String getCurrentMood() {
        return currentMood;
    }
}
