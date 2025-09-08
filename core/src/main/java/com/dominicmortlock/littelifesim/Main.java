package com.dominicmortlock.littelifesim;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Main game class that handles the core game loop and rendering.
 * Uses InputManager for input handling to keep this class focused on game logic.
 */
public class Main extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private Map map;
    private InputManager inputManager;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        map = new Map(GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        inputManager = new InputManager();
        
        // Create entities
        LittleGuy littleGuy = new LittleGuy(380, 270);
        map.addEntity(littleGuy);
        
        Ball ball = new Ball(200, 200);
        map.addEntity(ball);
        
        Bed bed = new Bed(600, 100);
        map.addEntity(bed);
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        
        // Handle mouse input
        inputManager.handleInput(map.getEntities());
        
        // Update game logic
        map.updateAll(deltaTime);
        
        // Render
        ScreenUtils.clear(1f, 1f, 1f, 1f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        map.renderAll(shapeRenderer);
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
