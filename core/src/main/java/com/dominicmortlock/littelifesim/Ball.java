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
    private float transitionDuration = 0.3f; // 0.3 seconds to move to hand
    private float startX, startY; // Starting position for transition
    private float targetX, targetY; // Target position for transition
    
    public Ball(float x, float y) {
        super(x, y, 20, 20, 1f); // 20x20 pixel ball, Z=1 (front layer)
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
            if (carrier instanceof Holder) {
                float[] targetPos = ((Holder) carrier).getHoldingPosition(this);
                targetX = targetPos[0];
                targetY = targetPos[1];
            } else {
                // Fallback for backwards compatibility
                targetX = carrier.getX() - width / 2; // Centered on left edge
                targetY = carrier.getY() + carrier.getHeight() / 2 - height / 2; // Halfway up
            }
            
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
            if (carrier instanceof Holder) {
                HoldingSystem.updateHeldPosition((Holder) carrier);
            } else {
                // Fallback to old positioning for backwards compatibility
                float carrierX = carrier.getX() - width / 2; // Centered on left edge
                float carrierY = carrier.getY() + carrier.getHeight() / 2 - height / 2; // Halfway up
                setPosition(carrierX, carrierY);
            }
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
        
        // Check all entities for nearby LittleGuy
        for (Entity entity : map.getEntities()) {
            if (entity instanceof LittleGuy) {
                LittleGuy littleGuy = (LittleGuy) entity;
                
                boolean isNear = isNearEntity(littleGuy, 40f); // Increase snap distance
                boolean canPickup = littleGuy.getCurrentState() == State.IDLE || 
                                   littleGuy.getCurrentState() == State.WALKING || 
                                   littleGuy.getCurrentState() == State.SLEEPING;
                
                // Check if ball is close enough and player can pick it up
                if (isNear && canPickup) {
                    System.out.println("=== BALL SNAP ATTACHMENT TRIGGERED ===");
                    // Use the holding system for instant attachment
                    HoldingSystem.startHolding(littleGuy, this);
                    break; // Only attach to one player
                }
            }
        }
    }
    
    private boolean isNearEntity(Entity entity, float margin) {
        return entity.getX() - margin < x + width &&
               entity.getX() + entity.getWidth() + margin > x &&
               entity.getY() - margin < y + height &&
               entity.getY() + entity.getHeight() + margin > y;
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
        return (carrier instanceof Holder) ? (Holder) carrier : null;
    }
    
    @Override
    public boolean isBeingHeld() {
        return carrier != null && currentState == BallState.CARRIED;
    }
}
