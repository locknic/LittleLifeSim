package com.dominicmortlock.littelifesim;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * A bed entity that LittleGuy can sleep in for extended periods.
 * Renders as a tall rectangle with a pillow at the top. Can be dragged around.
 */
public class Bed extends Entity implements Draggable {
    private boolean occupied;
    private Entity occupant;
    private DraggableComponent draggableComponent;
    private PhysicsComponent physicsComponent;
    
    // Visual constants  
    private static final float PILLOW_WIDTH = 30f;
    private static final float PILLOW_HEIGHT = 12f;
    private static final float PILLOW_OFFSET_Y = 5f;
    
    public Bed(float x, float y) {
        super(x, y, 60, 80, -1f); // Wider bed: 60 width × 80 height, Z=-1 (back layer)
        this.occupied = false;
        this.occupant = null;
        this.draggableComponent = new DraggableComponent(this);
        this.physicsComponent = new PhysicsComponent(this);
    }
    
    @Override
    public void update(float deltaTime) {
        draggableComponent.update(deltaTime);
        physicsComponent.update(deltaTime);
        
        // Check for player pickup when being dragged
        if (draggableComponent.isBeingDragged() && !occupied && map != null) {
            checkForPlayerPickup();
        }
    }
    
    @Override
    public void render(ShapeRenderer shapeRenderer) {
        // Use RenderUtils for rotation support
        float rotation = draggableComponent.getRotation();
        float pivotX = x + width / 2; // Center X
        float pivotY = y + height / 2; // Center Y
        
        // Draw bed frame (dark brown/black)
        shapeRenderer.setColor(0.2f, 0.1f, 0.05f, 1f); // Dark brown
        RenderUtils.renderRotatedRectangle(shapeRenderer, x, y, width, height, rotation, pivotX, pivotY);
        
        // Draw pillow (white rectangle at top of bed)
        float pillowX = x + (width - PILLOW_WIDTH) / 2; // Center horizontally
        float pillowY = y + height - PILLOW_HEIGHT - PILLOW_OFFSET_Y; // Near top
        
        shapeRenderer.setColor(1f, 1f, 1f, 1f); // White
        RenderUtils.renderRotatedRectangle(shapeRenderer, pillowX, pillowY, PILLOW_WIDTH, PILLOW_HEIGHT, rotation, pivotX, pivotY);
    }
    
    public boolean isOccupied() {
        return occupied;
    }
    
    public void setOccupied(boolean occupied, Entity occupant) {
        this.occupied = occupied;
        this.occupant = occupant;
    }
    
    public Entity getOccupant() {
        return occupant;
    }
    
    /**
     * Checks if an entity is close enough to interact with the bed
     */
    public boolean isNearby(Entity entity, float margin) {
        return entity.getX() - margin < x + width &&
               entity.getX() + entity.getWidth() + margin > x &&
               entity.getY() - margin < y + height &&
               entity.getY() + entity.getHeight() + margin > y;
    }
    
    /**
     * Get the sleeping position for an entity in this bed
     */
    public float[] getSleepingPosition(Entity entity) {
        // Center the entity in the bed
        float sleepX = x + (width - entity.getWidth()) / 2;
        float sleepY = y + (height - entity.getHeight()) / 2;
        return new float[]{sleepX, sleepY};
    }
    
    // Draggable interface implementation
    @Override
    public DraggableComponent getDraggableComponent() {
        return draggableComponent;
    }
    
    @Override
    public boolean isPointInside(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && 
               mouseY >= y && mouseY <= y + height;
    }
    
    @Override
    public void onDragStart() {
        // If occupied, wake up the occupant
        if (occupied && occupant != null) {
            if (occupant instanceof LittleGuy) {
                LittleGuy littleGuy = (LittleGuy) occupant;
                littleGuy.wakeUpFromBed();
            }
            setOccupied(false, null);
        }
        draggableComponent.startDrag();
    }
    
    @Override
    public void onDragStop() {
        // Get actual drag velocity for throwing
        float throwVelocityX = draggableComponent.getDragVelocityX();
        float throwVelocityY = draggableComponent.getDragVelocityY();
        
        // Scale velocity for bed throwing (lighter than ball)
        throwVelocityX *= 0.15f; 
        throwVelocityY *= 0.15f;
        
        draggableComponent.stopDrag();
        
        // Launch with physics if there's significant velocity
        if (Math.abs(throwVelocityX) > 20f || Math.abs(throwVelocityY) > 20f) {
            physicsComponent.launch(throwVelocityX, throwVelocityY);
        }
    }
    
    private void checkForPlayerPickup() {
        // Check all entities in the map for LittleGuy
        for (Entity entity : map.getEntities()) {
            if (entity instanceof LittleGuy) {
                LittleGuy littleGuy = (LittleGuy) entity;
                // Check if bed overlaps with player and player isn't already in a bed
                if (isNearby(littleGuy, 5f) && littleGuy.getCurrentBed() == null) {
                    pickupPlayer(littleGuy);
                    break; // Only pick up one player at a time
                }
            }
        }
    }
    
    private void pickupPlayer(LittleGuy littleGuy) {
        // Wake up the player if sleeping
        if (littleGuy.getCurrentState() == State.SLEEPING_IN_BED) {
            littleGuy.wakeUpFromBed();
        }
        
        // Set player to be carried by bed
        littleGuy.startBeingCarriedByBed(this);
        setOccupied(true, littleGuy);
    }
    
    @Override
    public void setMap(Map map) {
        super.setMap(map);
        physicsComponent.setMap(map);
    }
}
