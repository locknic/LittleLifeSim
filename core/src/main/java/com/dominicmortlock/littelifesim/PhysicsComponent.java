package com.dominicmortlock.littelifesim;

import com.badlogic.gdx.graphics.Color;

/**
 * Handles physics simulation for entities including gravity, bouncing, wall collisions,
 * and particle effects. Provides realistic movement and collision responses.
 */
public class PhysicsComponent {
    private Entity owner;
    private float velocityX;
    private float velocityY;
    private float groundLevel;
    private boolean hasPhysics;
    private Map map; // For boundary checking
    
    // Physics constants from GameConstants
    private static final float GRAVITY = GameConstants.GRAVITY;
    private static final float BOUNCE_DAMPING = GameConstants.BOUNCE_DAMPING;
    private static final float WALL_BOUNCE_DAMPING = GameConstants.WALL_BOUNCE_DAMPING;
    private static final float FRICTION = GameConstants.FRICTION;
    private static final float MIN_BOUNCE_VELOCITY = GameConstants.MIN_BOUNCE_VELOCITY;
    private static final float DUST_VELOCITY_THRESHOLD = GameConstants.DUST_VELOCITY_THRESHOLD;
    
    // Particle constants from GameConstants
    private static final int MAX_DUST_PARTICLES = GameConstants.MAX_DUST_PARTICLES;
    private static final float DUST_SPREAD_RADIUS = GameConstants.DUST_SPREAD_RADIUS;
    private static final float DUST_VERTICAL_SPREAD = GameConstants.DUST_VERTICAL_SPREAD;
    private static final float DUST_MIN_SPEED = GameConstants.DUST_MIN_SPEED;
    private static final float DUST_MAX_SPEED = GameConstants.DUST_MAX_SPEED;
    
    public PhysicsComponent(Entity owner) {
        this.owner = owner;
        this.velocityX = 0f;
        this.velocityY = 0f;
        this.groundLevel = owner.getY();
        this.hasPhysics = false;
        this.map = null;
    }
    
    public void update(float deltaTime) {
        if (!hasPhysics) return;
        
        // Apply gravity to Y velocity
        velocityY += GRAVITY * deltaTime;
        
        // Update position
        float newX = owner.getX() + velocityX * deltaTime;
        float newY = owner.getY() + velocityY * deltaTime;
        
        // Handle wall collisions
        if (map != null) {
            float[] newPosition = handleWallCollisions(newX, newY);
            newX = newPosition[0];
            newY = newPosition[1];
        }
        
        // Ground collision detection
        if (newY <= groundLevel && velocityY < 0) {
            newY = groundLevel;
            float bounceVelocity = Math.abs(velocityY);
            velocityY = -velocityY * BOUNCE_DAMPING; // Bounce with damping
            
            // Emit dust particles on ground bounce
            emitDustParticles(newX + owner.getWidth()/2, newY + owner.getHeight(), bounceVelocity);
            
            // Apply friction to horizontal movement
            velocityX *= FRICTION;
            
            // Stop bouncing if velocity is too small
            if (Math.abs(velocityY) < MIN_BOUNCE_VELOCITY) {
                velocityY = 0f;
                // Stop horizontal movement if very slow
                if (Math.abs(velocityX) < MIN_BOUNCE_VELOCITY) {
                    velocityX = 0f;
                    hasPhysics = false; // Stop physics when settled
                }
            }
        }
        
        owner.setPosition(newX, newY);
    }
    
    public void launch(float velocityX, float velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.groundLevel = owner.getY(); // Set current position as ground reference
        this.hasPhysics = true;
    }
    
    public void launch(float velocityX, float velocityY, float groundLevel) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.groundLevel = groundLevel;
        this.hasPhysics = true;
    }
    
    public void stop() {
        this.velocityX = 0f;
        this.velocityY = 0f;
        this.hasPhysics = false;
    }
    
    public void setMap(Map map) {
        this.map = map;
    }
    
    public boolean isActive() {
        return hasPhysics;
    }
    
    public float getVelocityX() {
        return velocityX;
    }
    
    public float getVelocityY() {
        return velocityY;
    }
    
    public void setGroundLevel(float groundLevel) {
        this.groundLevel = groundLevel;
    }
    
    private void emitDustParticles(float x, float y, float impactVelocity) {
        if (map == null || impactVelocity < DUST_VELOCITY_THRESHOLD) return;
        
        ParticleSystem particleSystem = map.getParticleSystem();
        if (particleSystem == null) return;
        
        // Emit dust particles based on impact velocity
        int particleCount = 1 + (int)(impactVelocity / 150f);
        particleCount = Math.min(particleCount, MAX_DUST_PARTICLES);
        
        for (int i = 0; i < particleCount; i++) {
            createDustParticle(particleSystem, x, y);
        }
    }
    
    private float[] handleWallCollisions(float newX, float newY) {
        // Left wall
        if (newX < 0 && velocityX < 0) {
            newX = 0;
            velocityX = -velocityX * WALL_BOUNCE_DAMPING;
            emitDustParticles(newX + owner.getWidth()/2, newY + owner.getHeight()/2, Math.abs(velocityX));
        }
        // Right wall
        else if (newX + owner.getWidth() > map.getWidth() && velocityX > 0) {
            newX = map.getWidth() - owner.getWidth();
            velocityX = -velocityX * WALL_BOUNCE_DAMPING;
            emitDustParticles(newX + owner.getWidth()/2, newY + owner.getHeight()/2, Math.abs(velocityX));
        }
        
        // Top wall (ceiling)
        if (newY + owner.getHeight() > map.getHeight() && velocityY > 0) {
            newY = map.getHeight() - owner.getHeight();
            velocityY = -velocityY * WALL_BOUNCE_DAMPING;
            emitDustParticles(newX + owner.getWidth()/2, newY + owner.getHeight()/2, Math.abs(velocityY));
        }
        
        // Bottom wall (floor boundary - different from ground level)
        if (newY < 0 && velocityY < 0) {
            newY = 0;
            velocityY = -velocityY * WALL_BOUNCE_DAMPING;
            emitDustParticles(newX + owner.getWidth()/2, newY + owner.getHeight()/2, Math.abs(velocityY));
        }
        
        return new float[]{newX, newY};
    }
    
    private void createDustParticle(ParticleSystem particleSystem, float x, float y) {
        // Random spread around impact point
        float particleX = x + (float)(Math.random() - 0.5) * DUST_SPREAD_RADIUS;
        float particleY = y + (float)(Math.random() - 0.5) * DUST_VERTICAL_SPREAD;
        
        // Random velocity outward from impact
        float angle = (float)(Math.random() * Math.PI * 2);
        float speed = DUST_MIN_SPEED + (float)Math.random() * (DUST_MAX_SPEED - DUST_MIN_SPEED);
        float velX = (float)Math.cos(angle) * speed;
        float velY = Math.abs((float)Math.sin(angle)) * speed * 0.4f; // Mostly upward
        
        // Create dust particle (fixed sizes, black)
        Color dustColor = new Color(0f, 0f, 0f, 0.8f);
        float particleSize = Math.random() < 0.5f ? 2f : 3f;
        Particle dustParticle = new Particle(
            particleX, particleY, 
            velX, velY,
            0.6f + (float)Math.random() * 0.3f, // 0.6-0.9 second lifetime
            particleSize,
            dustColor
        );
        
        particleSystem.addParticle(dustParticle);
    }
}
