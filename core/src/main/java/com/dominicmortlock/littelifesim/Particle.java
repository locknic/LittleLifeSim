package com.dominicmortlock.littelifesim;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.Random;

public class Particle {
    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private float lifetime;
    private float maxLifetime;
    private float size;
    private Color color;
    private boolean alive;
    private float groundLevel; // The level where particle should stop falling
    
    private static final float GRAVITY = -120f; // pixels per second squared (downward)
    private static final float BOUNCE_DAMPING = 0.3f; // How much velocity is retained after bounce
    
    public Particle(float x, float y, float velocityX, float velocityY, float lifetime, float size, Color color) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.lifetime = lifetime;
        this.maxLifetime = lifetime;
        this.size = size;
        this.color = new Color(color);
        this.alive = true;
        
        // Set ground level with some randomness around starting height
        Random random = new Random();
        this.groundLevel = y + (random.nextFloat() - 0.5f) * 20f; // ±10 pixels variation
        
        // Ensure ground level doesn't go below 0
        this.groundLevel = Math.max(0, this.groundLevel);
    }
    
    public void update(float deltaTime) {
        if (!alive) return;
        
        // Apply gravity to velocity
        velocityY += GRAVITY * deltaTime;
        
        // Update position
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;
        
        // Ground collision detection
        if (y <= groundLevel && velocityY < 0) {
            y = groundLevel; // Place on ground
            velocityY = -velocityY * BOUNCE_DAMPING; // Small bounce with damping
            
            // If bounce is too small, stop bouncing
            if (Math.abs(velocityY) < 10f) {
                velocityY = 0f;
            }
            
            // Reduce horizontal velocity when hitting ground (friction)
            velocityX *= 0.8f;
        }
        
        // Update lifetime
        lifetime -= deltaTime;
        if (lifetime <= 0) {
            alive = false;
        }
    }
    
    public void render(ShapeRenderer shapeRenderer) {
        if (!alive) return;
        
        // Calculate alpha based on remaining lifetime
        float alpha = lifetime / maxLifetime;
        shapeRenderer.setColor(color.r, color.g, color.b, alpha);
        shapeRenderer.rect(x - size/2, y - size/2, size, size);
    }
    
    public boolean isAlive() {
        return alive;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
}
