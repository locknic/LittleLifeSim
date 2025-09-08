package com.dominicmortlock.littelifesim;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Main game class that handles the core game loop, rendering, and input.
 * Manages the interaction between LittleGuy and Ball entities through dragging mechanics.
 */
public class Main extends ApplicationAdapter {
    // Window and game constants
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final float LITTLE_GUY_GRAB_RATIO = 0.8f;
    private static final float BALL_GRAB_RATIO = 0.5f;
    
    private ShapeRenderer shapeRenderer;
    private Map map;
    private LittleGuy littleGuy;
    private Ball ball;
    private Bed bed;
    private Draggable currentlyDragged;
    private boolean isDragging;
    private float dragOffsetX, dragOffsetY;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        map = new Map(WINDOW_WIDTH, WINDOW_HEIGHT);
        
        littleGuy = new LittleGuy(380, 270);
        map.addEntity(littleGuy);
        
        ball = new Ball(200, 200);
        map.addEntity(ball);
        
        bed = new Bed(600, 100);
        map.addEntity(bed);
        
        isDragging = false;
        currentlyDragged = null;
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        
        // Handle mouse input
        handleMouseInput();
        
        // Update game logic
        map.updateAll(deltaTime);
        
        // Render
        ScreenUtils.clear(1f, 1f, 1f, 1f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        map.renderAll(shapeRenderer);
        shapeRenderer.end();
    }
    
    private void handleMouseInput() {
        float mouseX = Gdx.input.getX();
        float mouseY = WINDOW_HEIGHT - Gdx.input.getY(); // Flip Y coordinate (LibGDX uses bottom-left origin)
        
        if (Gdx.input.justTouched()) {
            // Check draggable entities in priority order (ball first, then little guy, then bed)
            Draggable clickedEntity = null;
            
            // Check ball first (higher priority)
            if (ball.isPointInside(mouseX, mouseY)) {
                clickedEntity = ball;
            }
            // Then check little guy
            else if (littleGuy.isPointInside(mouseX, mouseY)) {
                clickedEntity = littleGuy;
            }
            // Then check bed
            else if (bed.isPointInside(mouseX, mouseY)) {
                clickedEntity = bed;
            }
            
            if (clickedEntity != null) {
                isDragging = true;
                currentlyDragged = clickedEntity;
                clickedEntity.onDragStart();
                
                // Snap entity position based on type
                snapEntityToMouse(clickedEntity, mouseX, mouseY);
            }
        }
        
        if (isDragging && currentlyDragged != null) {
            if (Gdx.input.isTouched()) {
                // Update dragged entity position
                updateDraggedEntityPosition(mouseX, mouseY);
            } else {
                // Mouse released - drop the entity
                isDragging = false;
                currentlyDragged.onDragStop();
                currentlyDragged = null;
            }
        }
    }

    private void snapEntityToMouse(Draggable entity, float mouseX, float mouseY) {
        if (entity == littleGuy) {
            // Snap little guy so mouse is center-x and at grab point
            float newX = mouseX - littleGuy.getWidth() / 2;
            float newY = mouseY - littleGuy.getHeight() * LITTLE_GUY_GRAB_RATIO;
            littleGuy.setPosition(newX, newY);
            dragOffsetX = littleGuy.getWidth() / 2;
            dragOffsetY = littleGuy.getHeight() * LITTLE_GUY_GRAB_RATIO;
        } else if (entity == ball) {
            // Snap ball so mouse is at center
            float newX = mouseX - ball.getWidth() * BALL_GRAB_RATIO;
            float newY = mouseY - ball.getHeight() * BALL_GRAB_RATIO;
            ball.setPosition(newX, newY);
            dragOffsetX = ball.getWidth() * BALL_GRAB_RATIO;
            dragOffsetY = ball.getHeight() * BALL_GRAB_RATIO;
        } else if (entity == bed) {
            // Snap bed so mouse is at center
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
        
        if (currentlyDragged == littleGuy) {
            littleGuy.setPosition(newX, newY);
        } else if (currentlyDragged == ball) {
            ball.setPosition(newX, newY);
        } else if (currentlyDragged == bed) {
            bed.setPosition(newX, newY);
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
