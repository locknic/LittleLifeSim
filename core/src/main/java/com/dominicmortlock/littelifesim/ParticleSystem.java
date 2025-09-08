package com.dominicmortlock.littelifesim;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages particles and particle emitters for visual effects.
 * Efficiently handles particle lifecycle and rendering.
 */
public class ParticleSystem {
    private List<Particle> particles;
    private List<ParticleEmitter> emitters;
    
    private static final int INITIAL_PARTICLE_CAPACITY = 50;
    private static final int INITIAL_EMITTER_CAPACITY = 10;
    
    public ParticleSystem() {
        particles = new ArrayList<>(INITIAL_PARTICLE_CAPACITY);
        emitters = new ArrayList<>(INITIAL_EMITTER_CAPACITY);
    }
    
    public void addParticle(Particle particle) {
        particles.add(particle);
    }
    
    public void addEmitter(ParticleEmitter emitter) {
        emitters.add(emitter);
    }
    
    public void removeEmitter(ParticleEmitter emitter) {
        emitters.remove(emitter);
    }
    
    public void update(float deltaTime) {
        // Update emitters
        for (ParticleEmitter emitter : emitters) {
            emitter.update(deltaTime, this);
        }
        
        // Update particles
        for (Particle particle : particles) {
            particle.update(deltaTime);
        }
        
        // Remove dead particles
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            if (!particle.isAlive()) {
                iterator.remove();
            }
        }
    }
    
    public void render(ShapeRenderer shapeRenderer) {
        for (Particle particle : particles) {
            particle.render(shapeRenderer);
        }
    }
    
    public int getParticleCount() {
        return particles.size();
    }
}
