package com.dominicmortlock.littelifesim;

import com.badlogic.gdx.Gdx;
import java.util.List;

/**
 * Handles all mouse input and drag operations for the game.
 * Centralizes input logic and reduces complexity in Main class.
 */
public class InputManager {
    private Draggable currentlyDragged;
    private boolean isDragging;
    private float dragOffsetX, dragOffsetY;
    
    public InputManager() {
        this.isDragging = false;
        this.currentlyDragged = null;
    }
    
    /**
     * Process mouse input and handle drag operations
     */
    public void handleInput(List<Entity> entities) {
        float mouseX = Gdx.input.getX();
        float mouseY = GameConstants.WINDOW_HEIGHT - Gdx.input.getY(); // Flip Y coordinate
        
        if (Gdx.input.justTouched()) {
            handleMouseClick(mouseX, mouseY, entities);
        }
        
        if (isDragging && currentlyDragged != null) {
            if (Gdx.input.isTouched()) {
                updateDraggedEntityPosition(mouseX, mouseY);
            } else {
                handleMouseRelease();
            }
        }
    }
    
    private void handleMouseClick(float mouseX, float mouseY, List<Entity> entities) {
        Draggable clickedEntity = findClickedEntity(mouseX, mouseY, entities);
        
        if (clickedEntity != null) {
            startDragging(clickedEntity, mouseX, mouseY);
        }
    }
    
    private Draggable findClickedEntity(float mouseX, float mouseY, List<Entity> entities) {
        // Check entities in priority order: Ball > LittleGuy (if in bed) > LittleGuy > Bed
        Ball ball = findEntityOfType(entities, Ball.class);
        LittleGuy littleGuy = findEntityOfType(entities, LittleGuy.class);
        Bed bed = findEntityOfType(entities, Bed.class);
        
        // Ball has highest priority
        if (ball != null && ball.isPointInside(mouseX, mouseY)) {
            return ball;
        }
        
        // Special case: if little guy is sleeping in bed, prioritize bed
        if (littleGuy != null && littleGuy.getCurrentState() == State.SLEEPING_IN_BED && 
            bed != null && bed.isPointInside(mouseX, mouseY)) {
            return bed;
        }
        
        // Then check little guy
        if (littleGuy != null && littleGuy.isPointInside(mouseX, mouseY)) {
            return littleGuy;
        }
        
        // Finally check bed
        if (bed != null && bed.isPointInside(mouseX, mouseY)) {
            return bed;
        }
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private <T extends Entity> T findEntityOfType(List<Entity> entities, Class<T> type) {
        for (Entity entity : entities) {
            if (type.isInstance(entity)) {
                return (T) entity;
            }
        }
        return null;
    }
    
    private void startDragging(Draggable entity, float mouseX, float mouseY) {
        isDragging = true;
        currentlyDragged = entity;
        entity.onDragStart();
        
        // Calculate drag offset based on entity type
        calculateDragOffset(entity, mouseX, mouseY);
    }
    
    private void calculateDragOffset(Draggable entity, float mouseX, float mouseY) {
        if (entity instanceof LittleGuy) {
            LittleGuy littleGuy = (LittleGuy) entity;
            float newX = mouseX - littleGuy.getWidth() / 2;
            float newY = mouseY - littleGuy.getHeight() * GameConstants.LITTLE_GUY_GRAB_RATIO;
            littleGuy.setPosition(newX, newY);
            dragOffsetX = littleGuy.getWidth() / 2;
            dragOffsetY = littleGuy.getHeight() * GameConstants.LITTLE_GUY_GRAB_RATIO;
        } else if (entity instanceof Ball) {
            Ball ball = (Ball) entity;
            float newX = mouseX - ball.getWidth() * GameConstants.BALL_GRAB_RATIO;
            float newY = mouseY - ball.getHeight() * GameConstants.BALL_GRAB_RATIO;
            ball.setPosition(newX, newY);
            dragOffsetX = ball.getWidth() * GameConstants.BALL_GRAB_RATIO;
            dragOffsetY = ball.getHeight() * GameConstants.BALL_GRAB_RATIO;
        } else if (entity instanceof Bed) {
            Bed bed = (Bed) entity;
            float newX = mouseX - bed.getWidth() / 2;
            float newY = mouseY - bed.getHeight() / 2;
            bed.setPosition(newX, newY);
            dragOffsetX = bed.getWidth() / 2;
            dragOffsetY = bed.getHeight() / 2;
        }
    }
    
    private void updateDraggedEntityPosition(float mouseX, float mouseY) {
        float newX = mouseX - dragOffsetX;
        float newY = mouseY - dragOffsetY;
        
        if (currentlyDragged instanceof Entity) {
            ((Entity) currentlyDragged).setPosition(newX, newY);
        }
    }
    
    private void handleMouseRelease() {
        isDragging = false;
        currentlyDragged.onDragStop();
        currentlyDragged = null;
    }
    
    public boolean isDragging() {
        return isDragging;
    }
    
    public Draggable getCurrentlyDragged() {
        return currentlyDragged;
    }
}
