package com.dominicmortlock.littelifesim;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class ThoughtBubble {
    private String text;
    private float x, y;
    private float lifetime;
    private float maxLifetime;
    private boolean active;
    private Entity owner;
    
    // Visual properties
    private static final float BUBBLE_WIDTH = 30f;
    private static final float BUBBLE_HEIGHT = 20f;
    private static final float OFFSET_X = 5f; // Offset from entity
    private static final float OFFSET_Y = 10f; // Above entity
    
    public ThoughtBubble(Entity owner) {
        this.owner = owner;
        this.text = "";
        this.lifetime = 0f;
        this.maxLifetime = 2f; // Default 2 seconds
        this.active = false;
    }
    
    public void show(String text, float duration) {
        this.text = text;
        this.maxLifetime = duration;
        this.lifetime = 0f;
        this.active = true;
        updatePosition();
    }
    
    public void show(String text) {
        show(text, 2f); // Default 2 seconds
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
            // Fade out in last 30% of lifetime
            float fadeProgress = (lifetime - maxLifetime * 0.7f) / (maxLifetime * 0.3f);
            alpha = 1f - fadeProgress;
        }
        
        // Draw bubble background (white with border)
        shapeRenderer.setColor(1f, 1f, 1f, alpha * 0.9f); // White background
        shapeRenderer.rect(x - BUBBLE_WIDTH/2, y, BUBBLE_WIDTH, BUBBLE_HEIGHT);
        
        // Draw bubble border
        shapeRenderer.setColor(0f, 0f, 0f, alpha); // Black border
        // Top border
        shapeRenderer.rect(x - BUBBLE_WIDTH/2, y + BUBBLE_HEIGHT - 1, BUBBLE_WIDTH, 1);
        // Bottom border  
        shapeRenderer.rect(x - BUBBLE_WIDTH/2, y, BUBBLE_WIDTH, 1);
        // Left border
        shapeRenderer.rect(x - BUBBLE_WIDTH/2, y, 1, BUBBLE_HEIGHT);
        // Right border
        shapeRenderer.rect(x + BUBBLE_WIDTH/2 - 1, y, 1, BUBBLE_HEIGHT);
        
        // Draw small tail pointing to character
        float tailX = x - 5f;
        float tailY = y - 3f;
        shapeRenderer.setColor(1f, 1f, 1f, alpha * 0.9f);
        shapeRenderer.rect(tailX, tailY, 3, 3);
        shapeRenderer.setColor(0f, 0f, 0f, alpha);
        shapeRenderer.rect(tailX, tailY + 2, 3, 1); // Tail border
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void hide() {
        active = false;
    }
    
    public String getText() {
        return text;
    }
}
