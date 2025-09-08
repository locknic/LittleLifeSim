package com.dominicmortlock.littelifesim;

import java.util.List;
import java.util.ArrayList;

/**
 * Utility class for common entity operations and queries.
 * Reduces code duplication across entity classes.
 */
public final class EntityManager {
    
    private EntityManager() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }
    
    /**
     * Find the first entity of a specific type in the entities list
     */
    @SuppressWarnings("unchecked")
    public static <T extends Entity> T findEntityOfType(List<Entity> entities, Class<T> type) {
        for (Entity entity : entities) {
            if (type.isInstance(entity)) {
                return (T) entity;
            }
        }
        return null;
    }
    
    /**
     * Find all entities of a specific type in the entities list
     */
    @SuppressWarnings("unchecked")
    public static <T extends Entity> List<T> findEntitiesOfType(List<Entity> entities, Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Entity entity : entities) {
            if (type.isInstance(entity)) {
                result.add((T) entity);
            }
        }
        return result;
    }
    
    /**
     * Check if two entities are intersecting with optional margin
     */
    public static boolean areIntersecting(Entity entity1, Entity entity2, float margin) {
        return entity1.getX() - margin < entity2.getX() + entity2.getWidth() &&
               entity1.getX() + entity1.getWidth() + margin > entity2.getX() &&
               entity1.getY() - margin < entity2.getY() + entity2.getHeight() &&
               entity1.getY() + entity1.getHeight() + margin > entity2.getY();
    }
    
    /**
     * Check if two entities are intersecting without margin
     */
    public static boolean areIntersecting(Entity entity1, Entity entity2) {
        return areIntersecting(entity1, entity2, 0f);
    }
    
    /**
     * Check if an entity is near another entity within a specified distance
     */
    public static boolean isNearby(Entity entity1, Entity entity2, float distance) {
        return areIntersecting(entity1, entity2, distance);
    }
    
    /**
     * Calculate the center position of an entity
     */
    public static float[] getCenter(Entity entity) {
        return new float[]{
            entity.getX() + entity.getWidth() / 2,
            entity.getY() + entity.getHeight() / 2
        };
    }
    
    /**
     * Calculate distance between two entities' centers
     */
    public static float getDistance(Entity entity1, Entity entity2) {
        float[] center1 = getCenter(entity1);
        float[] center2 = getCenter(entity2);
        float dx = center1[0] - center2[0];
        float dy = center1[1] - center2[1];
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Find nearby balls that can be picked up
     */
    public static Ball findNearbyPickupableBall(List<Entity> entities, Entity searcher, float distance) {
        for (Entity entity : entities) {
            if (entity instanceof Ball) {
                Ball ball = (Ball) entity;
                if (ball.isFree() && isNearby(searcher, ball, distance)) {
                    return ball;
                }
            }
        }
        return null;
    }
    
    /**
     * Find nearby unoccupied beds that can be used
     */
    public static Bed findNearbyAvailableBed(List<Entity> entities, Entity searcher, float distance) {
        for (Entity entity : entities) {
            if (entity instanceof Bed) {
                Bed bed = (Bed) entity;
                if (!bed.isOccupied() && 
                    !bed.getDraggableComponent().isBeingDragged() && 
                    isNearby(searcher, bed, distance)) {
                    return bed;
                }
            }
        }
        return null;
    }
    
    /**
     * Find nearby beds that can hold an entity (using holding system)
     */
    public static Bed findNearbyHoldableBed(List<Entity> entities, Entity searcher, float distance) {
        for (Entity entity : entities) {
            if (entity instanceof Bed) {
                Bed bed = (Bed) entity;
                if (!bed.isHolding() && 
                    !bed.getDraggableComponent().isBeingDragged() && 
                    isNearby(searcher, bed, distance)) {
                    return bed;
                }
            }
        }
        return null;
    }
    
    
    /**
     * Find nearby little guys that can pick up items
     */
    public static LittleGuy findNearbyAvailableLittleGuy(List<Entity> entities, Entity searcher, float distance) {
        for (Entity entity : entities) {
            if (entity instanceof LittleGuy) {
                LittleGuy littleGuy = (LittleGuy) entity;
                State state = littleGuy.getCurrentState();
                boolean canPickup = state == State.IDLE || state == State.WALKING || state == State.PONDERING;
                if (canPickup && isNearby(searcher, littleGuy, distance)) {
                    return littleGuy;
                }
            }
        }
        return null;
    }
}
