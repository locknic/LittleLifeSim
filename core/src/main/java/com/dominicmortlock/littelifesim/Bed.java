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
    
    // Visual constants  
    private static final float PILLOW_WIDTH = 50f;
    private static final float PILLOW_HEIGHT = 20f;
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
        
        // Removed player pickup during dragging - bed should only drop, not pick up
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
        System.out.println("=== BED DRAG START ===");
        System.out.println("Bed occupied: " + occupied);
        if (occupant != null) {
            System.out.println("Occupant: " + occupant.getClass().getSimpleName());
            if (occupant instanceof LittleGuy) {
                System.out.println("LittleGuy state: " + ((LittleGuy) occupant).getCurrentState());
            }
        }
        
        // Always release any occupant when bed is picked up (use new system)
        onHolderDragStart();
        // Also force-stop any player pickup behavior
        physicsComponent.stop(); // This will be called again in DragDropHelper, but ensure it's called
        DragDropHelper.onDragStart(physicsComponent, draggableComponent);
    }
    
    private void releaseOccupant() {
        System.out.println("=== BED RELEASE OCCUPANT ===");
        System.out.println("Occupied: " + occupied + ", Occupant: " + (occupant != null ? occupant.getClass().getSimpleName() : "null"));
        
        if (occupied && occupant != null) {
            if (occupant instanceof LittleGuy) {
                LittleGuy littleGuy = (LittleGuy) occupant;
                System.out.println("Releasing LittleGuy, current state: " + littleGuy.getCurrentState());
                
                // Force release regardless of current state
                littleGuy.releaseFromBed();
                
                // Drop player with physics (like how player drops ball)
                littleGuy.getPhysicsComponent().launch(
                    (float)(Math.random() - 0.5) * 200, // Random horizontal velocity -100 to +100
                    100 + (float)Math.random() * 100     // Upward velocity 100-200
                );
                
                System.out.println("Player dropped with physics from bed");
                System.out.println("After release, LittleGuy state: " + littleGuy.getCurrentState());
            }
            setOccupied(false, null);
            System.out.println("Bed now unoccupied");
        } else {
            System.out.println("No occupant to release");
        }
    }
    
    @Override
    public void onDragStop() {
        DragDropHelper.onDragStop(physicsComponent, draggableComponent, 
                                 DragDropHelper.VelocityScales.BED, 
                                 DragDropHelper.MinThrowVelocities.BED);
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
        System.out.println("=== BED PICKING UP PLAYER ===");
        System.out.println("Player state before pickup: " + littleGuy.getCurrentState());
        
        // Wake up the player if sleeping
        if (littleGuy.getCurrentState() == State.SLEEPING_IN_BED) {
            littleGuy.wakeUpFromBed();
        }
        
        // Set player to be carried by bed
        littleGuy.startBeingCarriedByBed(this);
        setOccupied(true, littleGuy);
        
        System.out.println("Player state after pickup: " + littleGuy.getCurrentState());
        System.out.println("Bed occupied: " + occupied);
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
            pickupPlayer((LittleGuy) holdable);
        }
    }
    
    @Override
    public void dropHeldEntity() {
        releaseOccupant();
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
        HoldingSystem.dropWithPhysics(this);
    }
}
