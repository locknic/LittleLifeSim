package com.dominicmortlock.littelifesim;

import com.badlogic.gdx.graphics.Color;
import java.util.Random;

public class TrailEmitter implements ParticleEmitter {
    private Entity entity;
    private float minEmissionRate; // minimum particles per second
    private float maxEmissionRate; // maximum particles per second
    private float currentEmissionInterval; // current interval between emissions
    private float timeSinceLastEmission;
    private boolean active;
    private Random random;
    private float lastX, lastY; // Track previous position for direction calculation
    
    private static final float PARTICLE_LIFETIME = 2f; // seconds (shorter for cleaner look)
    private static final Color TRAIL_COLOR = new Color(0f, 0f, 0f, 0.7f); // Black particles
    private static final float BASE_UPWARD_VELOCITY = 60f; // pixels per second upward
    private static final float OPPOSITE_VELOCITY_FACTOR = 0.8f; // how much to push opposite to movement
    
    public TrailEmitter(Entity entity, float minEmissionRate, float maxEmissionRate) {
        this.entity = entity;
        this.minEmissionRate = minEmissionRate;
        this.maxEmissionRate = maxEmissionRate;
        this.timeSinceLastEmission = 0f;
        this.active = true;
        this.random = new Random();
        this.lastX = entity.getX();
        this.lastY = entity.getY();
        this.currentEmissionInterval = calculateRandomInterval();
    }
    
    @Override
    public void update(float deltaTime, ParticleSystem particleSystem) {
        if (!active) return;
        
        timeSinceLastEmission += deltaTime;
        
        // Check if it's time to emit a particle
        if (timeSinceLastEmission >= currentEmissionInterval) {
            emitParticle(particleSystem);
            timeSinceLastEmission = 0f;
            // Pick a new random interval for next emission
            currentEmissionInterval = calculateRandomInterval();
        }
    }
    
    private float calculateRandomInterval() {
        float randomRate = minEmissionRate + random.nextFloat() * (maxEmissionRate - minEmissionRate);
        return 1f / randomRate; // Convert rate to interval
    }
    
    private void emitParticle(ParticleSystem particleSystem) {
        // Calculate movement direction
        float currentX = entity.getX();
        float currentY = entity.getY();
        float deltaX = currentX - lastX;
        float deltaY = currentY - lastY;
        
        // Normalize movement direction
        float movementMagnitude = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        float normalizedDx = 0f;
        float normalizedDy = 0f;
        
        if (movementMagnitude > 0.1f) { // Only if actually moving
            normalizedDx = deltaX / movementMagnitude;
            normalizedDy = deltaY / movementMagnitude;
        }
        
        // Emit particle from entity's feet area
        float particleX = currentX + entity.getWidth() / 2;
        float particleY = currentY; // Bottom of the entity (feet level)
        
        // Add some random offset around the feet
        particleX += (random.nextFloat() - 0.5f) * entity.getWidth() * 0.8f;
        particleY += random.nextFloat() * entity.getHeight() * 0.2f; // Slight vertical variation near feet
        
        // Calculate velocity: opposite to movement direction + upward + random
        float baseVelocityX = -normalizedDx * OPPOSITE_VELOCITY_FACTOR * 30f; // opposite to movement
        float baseVelocityY = BASE_UPWARD_VELOCITY; // upward
        
        // Add random variation
        float velocityX = baseVelocityX + (random.nextFloat() - 0.5f) * 40f;
        float velocityY = baseVelocityY + (random.nextFloat() - 0.5f) * 20f;
        
        // Fixed size options (either 2 or 3 pixels)
        float size = random.nextFloat() < 0.5f ? 2f : 3f;
        
        Particle particle = new Particle(
            particleX, particleY,
            velocityX, velocityY,
            PARTICLE_LIFETIME,
            size,
            TRAIL_COLOR
        );
        
        particleSystem.addParticle(particle);
        
        // Update last position for next frame
        lastX = currentX;
        lastY = currentY;
    }
    
    @Override
    public boolean isActive() {
        return active;
    }
    
    @Override
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public void setEmissionRateRange(float minEmissionRate, float maxEmissionRate) {
        this.minEmissionRate = minEmissionRate;
        this.maxEmissionRate = maxEmissionRate;
        this.currentEmissionInterval = calculateRandomInterval();
    }
}
