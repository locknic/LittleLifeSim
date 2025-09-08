package com.dominicmortlock.littelifesim;

/**
 * Interface for entities that can be held by other entities
 */
public interface Holdable {
    /**
     * Called when this entity starts being held by a holder
     * @param holder The entity that is now holding this one
     */
    void startBeingHeld(Holder holder);
    
    /**
     * Called when this entity is released from being held
     */
    void releaseFromHolder();
    
    /**
     * Called when this entity should be dropped with physics
     */
    void dropWithPhysics();
    
    /**
     * Get the current holder of this entity
     * @return The holder, or null if not being held
     */
    Holder getCurrentHolder();
    
    /**
     * Check if this entity is currently being held
     * @return true if being held, false otherwise
     */
    boolean isBeingHeld();
}
