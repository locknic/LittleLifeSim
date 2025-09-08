package com.dominicmortlock.littelifesim;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public abstract class Entity {
    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected float z; // Z-axis for rendering order (higher = front)
    protected Map map;
    
    public Entity(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.z = 0f; // Default z-level
    }
    
    public Entity(float x, float y, float width, float height, float z) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.z = z;
    }
    
    public abstract void update(float deltaTime);
    public abstract void render(ShapeRenderer shapeRenderer);
    
    public void setMap(Map map) {
        this.map = map;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public float getWidth() {
        return width;
    }
    
    public float getHeight() {
        return height;
    }
    
    public float getZ() {
        return z;
    }
    
    public void setZ(float z) {
        this.z = z;
    }

    public void setPosition(float x, float y) {
        if (map != null) {
            x = Math.max(0, Math.min(x, map.getWidth() - width));
            y = Math.max(0, Math.min(y, map.getHeight() - height));
        }
        this.x = x;
        this.y = y;
    }
}
