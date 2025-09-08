package com.dominicmortlock.littelifesim;

/**
 * Helper class for standardizing drag-drop behavior across entities.
 * Reduces code duplication and ensures consistent behavior.
 */
public class DragDropHelper {
    
    /**
     * Handles common physics setup when an entity is picked up
     */
    public static void onDragStart(PhysicsComponent physics, DraggableComponent draggable) {
        if (physics != null) {
            physics.stop(); // Stop any physics when picked up
        }
        if (draggable != null) {
            draggable.startDrag();
        }
    }
    
    /**
     * Handles common physics and throwing when an entity is dropped
     */
    public static void onDragStop(PhysicsComponent physics, DraggableComponent draggable, 
                                 float velocityScale, float minThrowVelocity) {
        if (draggable == null) return;
        
        // Get actual drag velocity for throwing
        float throwVelocityX = draggable.getDragVelocityX();
        float throwVelocityY = draggable.getDragVelocityY();
        
        // Scale velocity for different entity types
        throwVelocityX *= velocityScale;
        throwVelocityY *= velocityScale;
        
        // Ensure minimum upward velocity for satisfying throws
        if (throwVelocityY < minThrowVelocity) {
            throwVelocityY = minThrowVelocity + (float) Math.random() * (minThrowVelocity * 0.5f);
        }
        
        draggable.stopDrag();
        
        // Launch with physics if there's significant velocity
        if (physics != null && (Math.abs(throwVelocityX) > 20f || Math.abs(throwVelocityY) > 20f)) {
            physics.launch(throwVelocityX, throwVelocityY);
        }
    }
    
    /**
     * Standard velocity scales for different entity types
     */
    public static class VelocityScales {
        public static final float BALL = 0.27f;
        public static final float LITTLE_GUY = 0.03f;
        public static final float BED = 0.12f; // Heavy furniture, moderately throwable
    }
    
    /**
     * Standard minimum throw velocities for different entity types
     */
    public static class MinThrowVelocities {
        public static final float BALL = 50f;
        public static final float LITTLE_GUY = 60f;
        public static final float BED = 40f; // Moderate minimum throw velocity for furniture
    }
}
