package com.dominicmortlock.littelifesim;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * A physics-enabled ball that can be dragged, thrown, caught by LittleGuy, and bounces around.
 * Supports multiple states including free, picked up, carried, and being caught.
 */
public class Ball extends Entity implements Draggable, Holdable {
    private BallState currentState;
    private DraggableComponent draggableComponent;
    private PhysicsComponent physicsComponent;
    private Entity carrier; // The entity carrying this ball (if any)
    
    // Smooth pickup transition
    private float transitionTimer;
    private float transitionDuration = GameConstants.BALL_TRANSITION_DURATION;
    private float startX, startY; // Starting position for transition
    private float targetX, targetY; // Target position for transition
    
    public Ball(float x, float y) {
        super(x, y, GameConstants.BALL_SIZE, GameConstants.BALL_SIZE, GameConstants.Z_FRONT_LAYER);
        this.currentState = BallState.FREE;
        this.draggableComponent = new DraggableComponent(this);
        this.physicsComponent = new PhysicsComponent(this);
    }
    
    @Override
    public void update(float deltaTime) {
        // Update physics component first
        physicsComponent.update(deltaTime);
        
        // Update draggable component
        draggableComponent.update(deltaTime);
        
        // Update drag physics if being dragged
        if (currentState == BallState.PICKED_UP) {
            draggableComponent.updateDrag(deltaTime);
        }
        
        // Handle being caught transition
        if (currentState == BallState.BEING_CAUGHT && carrier != null) {
            transitionTimer += deltaTime;
            float progress = Math.min(transitionTimer / transitionDuration, 1.0f);
            
            // Calculate target position using holding system
            float[] targetPos = ((Holder) carrier).getHoldingPosition(this);
            targetX = targetPos[0];
            targetY = targetPos[1];
            
            // Smooth interpolation with easing
            float easeProgress = 1f - (1f - progress) * (1f - progress); // Ease out
            float currentX = startX + (targetX - startX) * easeProgress;
            float currentY = startY + (targetY - startY) * easeProgress;
            
            setPosition(currentX, currentY);
            
            // Transition complete
            if (progress >= 1.0f) {
                currentState = BallState.CARRIED;
            }
        }
        
        // Update position if being carried (only if not in physics mode)
        if (currentState == BallState.CARRIED && carrier != null && !physicsComponent.isActive()) {
            // Use unified holding system for positioning
            HoldingSystem.updateHeldPosition((Holder) carrier);
        }
        
        // Check for snap attachment every frame if ball is free and physics isn't active
        if (currentState == BallState.FREE && !physicsComponent.isActive()) {
            checkForImmediatePlayerAttachment();
        }
    }
    
    @Override
    public void render(ShapeRenderer shapeRenderer) {
        // Change color based on state
        if (currentState == BallState.PICKED_UP) {
            shapeRenderer.setColor(0.8f, 0.4f, 0.4f, 1f); // Lighter red when picked up
        } else if (currentState == BallState.CARRIED) {
            shapeRenderer.setColor(0.7f, 0.2f, 0.2f, 1f); // Slightly lighter red when carried
        } else {
            shapeRenderer.setColor(0.6f, 0.1f, 0.1f, 1f); // Dark red when free
        }
        
        // Draw rotated ball around center point
        float pivotX = x + width / 2; // Center X
        float pivotY = y + height / 2; // Center Y (balls rotate around center)
        
        RenderUtils.renderRotatedRectangle(shapeRenderer, x, y, width, height, 
                                         draggableComponent.getRotation(), pivotX, pivotY);
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
        if (currentState == BallState.CARRIED) {
            // Remove from carrier
            carrier = null;
        }
        currentState = BallState.PICKED_UP;
        DragDropHelper.onDragStart(physicsComponent, draggableComponent);
    }
    
    @Override
    public void onDragStop() {
        currentState = BallState.FREE;
        DragDropHelper.onDragStop(physicsComponent, draggableComponent, 
                                 DragDropHelper.VelocityScales.BALL, 
                                 DragDropHelper.MinThrowVelocities.BALL);
        
        // Check for immediate attachment to nearby player
        checkForImmediatePlayerAttachment();
    }
    
    private void checkForImmediatePlayerAttachment() {
        if (map == null || currentState != BallState.FREE) return;
        
        LittleGuy nearbyLittleGuy = EntityManager.findNearbyAvailableLittleGuy(
            map.getEntities(), this, GameConstants.BALL_SNAP_DISTANCE);
        if (nearbyLittleGuy != null) {
            HoldingSystem.startHolding(nearbyLittleGuy, this);
        }
    }
    
    
    // Ball-specific methods
    public void startCarried(Entity carrier) {
        this.carrier = carrier;
        
        // Start smooth transition
        this.currentState = BallState.BEING_CAUGHT;
        this.transitionTimer = 0f;
        this.startX = getX();
        this.startY = getY();
        
        // Stop any physics and dragging
        physicsComponent.stop();
        draggableComponent.stopDrag();
    }
    
    public void stopCarried() {
        this.carrier = null;
        this.currentState = BallState.FREE;
    }
    
    public void throwBall(float velocityX, float velocityY) {
        // Drop from carrier if being carried
        if (currentState == BallState.CARRIED) {
            carrier = null;
        }
        
        currentState = BallState.FREE;
        physicsComponent.launch(velocityX, velocityY);
    }
    
    public void dropWithArc() {
        // Drop with small upward arc
        float dropVelocityX = (float) (Math.random() - 0.5) * 50f; // Small horizontal spread
        float dropVelocityY = 80f + (float) Math.random() * 40f; // Upward arc
        throwBall(dropVelocityX, dropVelocityY);
    }
    
    @Override
    public void setMap(Map map) {
        super.setMap(map);
        physicsComponent.setMap(map);
    }
    
    public boolean isCarried() {
        return currentState == BallState.CARRIED;
    }
    
    public boolean isFree() {
        return currentState == BallState.FREE;
    }
    
    public BallState getCurrentState() {
        return currentState;
    }
    
    // Holdable interface implementation
    @Override
    public void startBeingHeld(Holder holder) {
        if (holder instanceof Entity) {
            startCarried((Entity) holder);
        }
    }
    
    @Override
    public void releaseFromHolder() {
        stopCarried();
    }
    
    @Override
    public void dropWithPhysics() {
        dropWithArc();
    }
    
    @Override
    public Holder getCurrentHolder() {
        return (Holder) carrier; // Carriers (LittleGuy) implement Holder
    }
    
    @Override
    public boolean isBeingHeld() {
        return carrier != null && currentState == BallState.CARRIED;
    }
}
