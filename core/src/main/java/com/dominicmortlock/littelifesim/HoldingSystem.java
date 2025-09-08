package com.dominicmortlock.littelifesim;

/**
 * Utility class for managing holding relationships between entities
 */
public class HoldingSystem {
    
    /**
     * Establish a holding relationship between holder and holdable
     */
    public static void startHolding(Holder holder, Holdable holdable) {
        // Drop any existing held entity
        if (holder.isHolding()) {
            holder.dropHeldEntity();
        }
        
        // Release holdable from any existing holder
        if (holdable.isBeingHeld()) {
            holdable.releaseFromHolder();
        }
        
        // Establish the relationship
        holder.pickupHoldable(holdable);
        holdable.startBeingHeld(holder);
    }
    
    /**
     * Break a holding relationship
     */
    public static void stopHolding(Holder holder, Holdable holdable) {
        if (holder != null && holder.getHeldEntity() == holdable) {
            holder.dropHeldEntity();
        }
        
        if (holdable != null && holdable.getCurrentHolder() == holder) {
            holdable.releaseFromHolder();
        }
    }
    
    /**
     * Drop held entity with physics
     */
    public static void dropWithPhysics(Holder holder) {
        if (holder.isHolding()) {
            Holdable held = holder.getHeldEntity();
            stopHolding(holder, held);
            held.dropWithPhysics();
        }
    }
    
    /**
     * Update position of held entity to follow holder
     */
    public static void updateHeldPosition(Holder holder) {
        if (holder.isHolding()) {
            Holdable held = holder.getHeldEntity();
            float[] position = holder.getHoldingPosition(held);
            
            if (held instanceof Entity) {
                Entity entity = (Entity) held;
                entity.setPosition(position[0], position[1]);
            }
        }
    }
    
    /**
     * Check if two entities can establish a holding relationship
     */
    public static boolean canHold(Holder holder, Holdable holdable) {
        // Can't hold yourself
        if (holder == holdable) {
            return false;
        }
        
        // Both must exist
        if (holder == null || holdable == null) {
            return false;
        }
        
        // If holdable is also a holder, prevent circular holding
        if (holdable instanceof Holder) {
            Holder holdableAsHolder = (Holder) holdable;
            if (holdableAsHolder.isHolding() && holdableAsHolder.getHeldEntity() == holder) {
                return false;
            }
        }
        
        return true;
    }
}
