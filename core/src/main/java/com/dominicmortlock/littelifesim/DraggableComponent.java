package com.dominicmortlock.littelifesim;

import java.util.Random;

public class DraggableComponent {
    private Entity owner;
    private boolean isBeingDragged;
    private boolean wasBeingDragged;
    
    // Physics for dangling/swaying
    private float rotation; // Current rotation angle in radians
    private float angularVelocity; // Angular velocity in radians per second
    private float lastDragX, lastDragY; // Previous drag position for velocity calculation
    private float yVelocity; // Vertical velocity for pickup/drop physics
    private float targetY; // Ground reference for bouncing
    private float dragVelocityX, dragVelocityY; // Current drag velocity for throwing
    private float[] velocityHistoryX = new float[5]; // Track last 5 velocity samples
    private float[] velocityHistoryY = new float[5];
    private int velocityHistoryIndex = 0;
    
    private Random random;
    
    // Physics constants from GameConstants
    private static final float ROTATION_SENSITIVITY = GameConstants.ROTATION_SENSITIVITY;
    private static final float ANGULAR_DAMPING = GameConstants.ANGULAR_DAMPING;
    private static final float GRAVITY_TORQUE = GameConstants.GRAVITY_TORQUE;
    private static final float MAX_ROTATION = GameConstants.MAX_ROTATION;
    private static final float DROP_FALL_VELOCITY = GameConstants.DROP_FALL_VELOCITY;
    
    public DraggableComponent(Entity owner) {
        this.owner = owner;
        this.isBeingDragged = false;
        this.wasBeingDragged = false;
        this.rotation = 0f;
        this.angularVelocity = 0f;
        this.lastDragX = owner.getX();
        this.lastDragY = owner.getY();
        this.yVelocity = 0f;
        this.targetY = owner.getY();
        this.random = new Random();
    }
    
    public void update(float deltaTime) {
        updatePhysics(deltaTime);
    }
    
    private void updatePhysics(float deltaTime) {
        if (!isBeingDragged) {
            // Proper pendulum physics with restoring force
            float restoring_force = -rotation * GRAVITY_TORQUE;
            angularVelocity += restoring_force * deltaTime;
            
            // If we just stopped being dragged, ensure we have enough momentum for oscillation
            if (wasBeingDragged) {
                wasBeingDragged = false;
                // Add extra momentum if current angular velocity is too small for good oscillation
                if (Math.abs(angularVelocity) < 5f) {
                    angularVelocity += Math.signum(rotation) * -8f; // Boost in opposite direction
                }
            }
            
            // Apply Y velocity physics (falling/bouncing)
            if (Math.abs(yVelocity) > 0.1f) {
                // Apply gravity to Y velocity
                yVelocity -= 300f * deltaTime; // Gravity acceleration
                
                // Update Y position
                float newY = owner.getY() + yVelocity * deltaTime;
                
                // Check if we hit the ground (original Y position or map boundary)
                float groundLevel = Math.max(0, targetY); // Use targetY as ground reference
                if (newY <= groundLevel && yVelocity < 0) {
                    newY = groundLevel;
                    yVelocity = -yVelocity * 0.4f; // Bounce with damping
                    
                    // Stop bouncing if velocity is too small
                    if (Math.abs(yVelocity) < 20f) {
                        yVelocity = 0f;
                    }
                }
                
                owner.setPosition(owner.getX(), newY);
            }
        }
        
        // Update rotation first
        rotation += angularVelocity * deltaTime;
        
        // Apply damping after rotation update for better oscillation control
        angularVelocity *= ANGULAR_DAMPING;
        
        // Allow rotation beyond normal limits during pendulum swing for overshoot
        float swingLimit = MAX_ROTATION * 2f; // Allow more overshoot for better pendulum motion
        rotation = Math.max(-swingLimit, Math.min(swingLimit, rotation));
        
        // Stop oscillations only when both rotation and velocity are very small
        if (Math.abs(rotation) < 0.01f && Math.abs(angularVelocity) < 0.1f) {
            rotation = 0f;
            angularVelocity = 0f;
        }
    }
    
    public void startDrag() {
        isBeingDragged = true;
        
        // Add initial rotation impulse when picked up to make clicking feel impactful
        float randomRotationImpulse = (random.nextFloat() - 0.5f) * 8f; // Random rotation boost
        angularVelocity += randomRotationImpulse;
        
        // Reset Y velocity since we're snapping to position
        yVelocity = 0f;
    }
    
    public void updateDrag(float deltaTime) {
        if (!isBeingDragged) return;
        
        // Calculate current frame velocity
        float currentVelX = (owner.getX() - lastDragX) / deltaTime;
        float currentVelY = (owner.getY() - lastDragY) / deltaTime;
        
        // Store in velocity history for smoothing
        velocityHistoryX[velocityHistoryIndex] = currentVelX;
        velocityHistoryY[velocityHistoryIndex] = currentVelY;
        velocityHistoryIndex = (velocityHistoryIndex + 1) % velocityHistoryX.length;
        
        // Calculate smoothed velocity (average of recent samples)
        float sumX = 0f, sumY = 0f;
        int samples = 0;
        for (int i = 0; i < velocityHistoryX.length; i++) {
            if (velocityHistoryX[i] != 0f || velocityHistoryY[i] != 0f) {
                sumX += velocityHistoryX[i];
                sumY += velocityHistoryY[i];
                samples++;
            }
        }
        
        if (samples > 0) {
            dragVelocityX = sumX / samples;
            dragVelocityY = sumY / samples;
        } else {
            dragVelocityX = currentVelX;
            dragVelocityY = currentVelY;
        }
        
        // Apply rotation based on horizontal drag velocity
        float targetRotation = -dragVelocityX * ROTATION_SENSITIVITY;
        targetRotation = Math.max(-MAX_ROTATION, Math.min(MAX_ROTATION, targetRotation));
        
        // Add angular velocity toward target rotation
        float rotationDiff = targetRotation - rotation;
        angularVelocity += rotationDiff * 80f * deltaTime; // Strong spring force for immediate response
        
        // Update last drag position
        lastDragX = owner.getX();
        lastDragY = owner.getY();
        wasBeingDragged = true;
    }
    
    public void stopDrag() {
        if (!isBeingDragged) return;
        
        isBeingDragged = false;
        
        // Store current position as target for ground reference
        targetY = owner.getY();
        
        // Add downward velocity when dropped
        yVelocity = DROP_FALL_VELOCITY;
        
        // Reset rotation almost instantaneously
        rotation = 0f;
        angularVelocity = 0f;
        
        // Clear velocity history for next drag session
        for (int i = 0; i < velocityHistoryX.length; i++) {
            velocityHistoryX[i] = 0f;
            velocityHistoryY[i] = 0f;
        }
        velocityHistoryIndex = 0;
    }
    
    public float getRotation() {
        return rotation;
    }
    
    public boolean isBeingDragged() {
        return isBeingDragged;
    }
    
    public float getDragVelocityX() {
        return dragVelocityX;
    }
    
    public float getDragVelocityY() {
        return dragVelocityY;
    }
}
