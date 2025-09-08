package com.dominicmortlock.littelifesim;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * A physics-enabled ball that can be dragged, thrown, caught by LittleGuy, and bounces around.
 * Supports multiple states including free, picked up, carried, and being caught.
 */
public class Ball extends Entity implements Draggable {
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
            
            // Calculate target position
            targetX = carrier.getX() - width / 2; // Centered on left edge
            targetY = carrier.getY() + carrier.getHeight() / 2 - height / 2; // Halfway up
            
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
            // Position ball centered on left side of carrier, halfway up
            float carrierX = carrier.getX() - width / 2; // Centered on left edge
            float carrierY = carrier.getY() + carrier.getHeight() / 2 - height / 2; // Halfway up
            setPosition(carrierX, carrierY);
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
        physicsComponent.stop(); // Stop any physics when picked up
        draggableComponent.startDrag();
    }
    
    @Override
    public void onDragStop() {
        // Get actual drag velocity for throwing
        float throwVelocityX = draggableComponent.getDragVelocityX();
        float throwVelocityY = draggableComponent.getDragVelocityY();
        
        // Scale velocity for more realistic throwing (reduce excessive speeds)
        throwVelocityX *= 0.27f; // About 1/3 power
        throwVelocityY *= 0.27f;
        
        // Ensure minimum upward velocity for satisfying throws
        if (throwVelocityY < 50f) {
            throwVelocityY = 50f + (float) Math.random() * 50f;
        }
        
        currentState = BallState.FREE;
        draggableComponent.stopDrag();
        
        // Launch with physics if there's significant velocity
        if (Math.abs(throwVelocityX) > 20f || Math.abs(throwVelocityY) > 20f) {
            physicsComponent.launch(throwVelocityX, throwVelocityY);
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
}
