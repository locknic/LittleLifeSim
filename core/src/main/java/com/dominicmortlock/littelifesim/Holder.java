package com.dominicmortlock.littelifesim;

/**
 * Interface for entities that can hold other entities
 */
public interface Holder {
    /**
     * Pick up and start holding a holdable entity
     * @param holdable The entity to hold
     */
    void pickupHoldable(Holdable holdable);
    
    /**
     * Drop the currently held entity
     */
    void dropHeldEntity();
    
    /**
     * Get the currently held entity
     * @return The held entity, or null if not holding anything
     */
    Holdable getHeldEntity();
    
    /**
     * Check if this holder is currently holding something
     * @return true if holding an entity, false otherwise
     */
    boolean isHolding();
    
    /**
     * Get the position where the held entity should be positioned
     * @param holdable The entity being held
     * @return Array of [x, y] coordinates
     */
    float[] getHoldingPosition(Holdable holdable);
    
    /**
     * Called when the holder starts being dragged - should drop held entity
     */
    void onHolderDragStart();
}
