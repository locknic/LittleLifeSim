package com.dominicmortlock.littelifesim;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * A bed entity that LittleGuy can sleep in for extended periods.
 * Renders as a tall rectangle with a pillow at the top. Can be dragged around.
 */
public class Bed extends Entity implements Draggable, Holder {
    private boolean occupied;
    private Entity occupant;
    private DraggableComponent draggableComponent;
    private PhysicsComponent physicsComponent;
    
    // Visual constants from GameConstants
    private static final float PILLOW_WIDTH = GameConstants.PILLOW_WIDTH;
    private static final float PILLOW_HEIGHT = GameConstants.PILLOW_HEIGHT;
    private static final float PILLOW_OFFSET_Y = GameConstants.PILLOW_OFFSET_Y;
    
    public Bed(float x, float y) {
        super(x, y, GameConstants.BED_WIDTH, GameConstants.BED_HEIGHT, GameConstants.Z_BACK_LAYER);
        this.occupied = false;
        this.occupant = null;
        this.draggableComponent = new DraggableComponent(this);
        this.physicsComponent = new PhysicsComponent(this);
    }
    
    @Override
    public void update(float deltaTime) {
        draggableComponent.update(deltaTime);
        physicsComponent.update(deltaTime);
        
        // Update drag physics if being dragged (same pattern as ball and player)
        if (draggableComponent.isBeingDragged()) {
            draggableComponent.updateDrag(deltaTime);
        }
    }
    
    @Override
    public void render(ShapeRenderer shapeRenderer) {
        // Use RenderUtils for rotation support
        float rotation = draggableComponent.getRotation();
        float pivotX = x + width / 2; // Center X
        float pivotY = y + height * 0.8f; // Upper portion (headboard grab point)
        
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
        return EntityManager.isNearby(entity, this, margin);
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
        // Wake up any occupant when bed is picked up
        if (occupied && occupant instanceof LittleGuy) {
            ((LittleGuy) occupant).wakeUpFromBed();
        }
        DragDropHelper.onDragStart(physicsComponent, draggableComponent);
    }
    
    private void releaseOccupant() {
        if (occupied && occupant != null) {
            if (occupant instanceof LittleGuy) {
                LittleGuy littleGuy = (LittleGuy) occupant;
                
                // Force release regardless of current state
                littleGuy.releaseFromBed();
                
                // Drop player with physics (like how player drops ball)
                littleGuy.getPhysicsComponent().launch(
                    (float)(Math.random() - 0.5) * 200, // Random horizontal velocity -100 to +100
                    100 + (float)Math.random() * 100     // Upward velocity 100-200
                );
            }
            setOccupied(false, null);
        }
    }
    
    @Override
    public void onDragStop() {
        DragDropHelper.onDragStop(physicsComponent, draggableComponent, 
                                 DragDropHelper.VelocityScales.BED, 
                                 DragDropHelper.MinThrowVelocities.BED);
    }
    
    
    
    @Override
    public void setMap(Map map) {
        super.setMap(map);
        physicsComponent.setMap(map);
    }
    
    // Holder interface implementation
    @Override
    public void pickupHoldable(Holdable holdable) {
        if (holdable instanceof LittleGuy) {
            LittleGuy littleGuy = (LittleGuy) holdable;
            littleGuy.startSleepingInBed(this);
        }
    }
    
    @Override
    public void dropHeldEntity() {
        if (occupied && occupant instanceof LittleGuy) {
            ((LittleGuy) occupant).wakeUpFromBed();
        }
    }
    
    @Override
    public Holdable getHeldEntity() {
        return (occupant instanceof Holdable) ? (Holdable) occupant : null;
    }
    
    @Override
    public boolean isHolding() {
        return occupied && occupant != null;
    }
    
    @Override
    public float[] getHoldingPosition(Holdable holdable) {
        return getSleepingPosition((Entity) holdable);
    }
    
    @Override
    public void onHolderDragStart() {
        // Wake up any sleeping occupant when bed is dragged
        dropHeldEntity();
    }
}
