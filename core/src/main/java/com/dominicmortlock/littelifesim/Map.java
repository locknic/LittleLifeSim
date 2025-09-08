package com.dominicmortlock.littelifesim;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Map {
    private List<Entity> entities;
    private ParticleSystem particleSystem;
    private float width;
    private float height;
    
    public Map(float width, float height) {
        this.width = width;
        this.height = height;
        entities = new ArrayList<>();
        particleSystem = new ParticleSystem();
    }
    
    public void addEntity(Entity entity) {
        entity.setMap(this);
        entities.add(entity);
    }
    
    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }
    
    public List<Entity> getEntities() {
        return entities;
    }
    
    public void updateAll(float deltaTime) {
        for (Entity entity : entities) {
            entity.update(deltaTime);
        }
        particleSystem.update(deltaTime);
    }
    
    public void renderAll(ShapeRenderer shapeRenderer) {
        // Render particles first (behind entities)
        particleSystem.render(shapeRenderer);
        
        // Sort entities by Z-axis (back to front: lower Z first)
        entities.sort(Comparator.comparing(Entity::getZ));
        
        // Then render entities in Z-order
        for (Entity entity : entities) {
            entity.render(shapeRenderer);
        }
    }
    
    public float getWidth() {
        return width;
    }
    
    public float getHeight() {
        return height;
    }
    
    public ParticleSystem getParticleSystem() {
        return particleSystem;
    }
}
