package com.dominicmortlock.littelifesim;

/**
 * Centralized constants for the game to improve maintainability and consistency.
 */
public final class GameConstants {
    
    // Window and game dimensions
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;
    
    // Entity dimensions
    public static final float LITTLE_GUY_WIDTH = 40f;
    public static final float LITTLE_GUY_HEIGHT = 60f;
    public static final float BALL_SIZE = 20f;
    public static final float BED_WIDTH = 60f;
    public static final float BED_HEIGHT = 80f;
    
    // Z-order layers
    public static final float Z_BACK_LAYER = -1f;   // Beds
    public static final float Z_MIDDLE_LAYER = 0f;  // LittleGuy
    public static final float Z_FRONT_LAYER = 1f;   // Balls
    
    // Drag interaction constants
    public static final float LITTLE_GUY_GRAB_RATIO = 0.8f;
    public static final float BALL_GRAB_RATIO = 0.5f;
    
    // Physics constants
    public static final float GRAVITY = -300f;
    public static final float BOUNCE_DAMPING = 0.6f;
    public static final float WALL_BOUNCE_DAMPING = 0.5f;
    public static final float FRICTION = 0.8f;
    public static final float MIN_BOUNCE_VELOCITY = 20f;
    
    // Movement and timing
    public static final float WALK_SPEED = 50f;
    public static final float MIN_IDLE_TIME = 3f;
    public static final float MAX_IDLE_TIME = 10f;
    public static final float MIN_PONDER_TIME = 8f;
    public static final float MAX_PONDER_TIME = 20f;
    public static final float PICKUP_COOLDOWN_TIME = 2f;
    public static final float THROW_ANIMATION_TIME = 0.5f;
    public static final float BED_SLEEP_TIME = 60f;
    public static final float BED_COOLDOWN_TIME = 10f;
    
    // Interaction distances
    public static final float COLLISION_MARGIN = 5f;
    public static final float BED_INTERACTION_MARGIN = 35f; // Match snap distance for consistency
    public static final float SNAP_DISTANCE = 35f;
    public static final float BALL_SNAP_DISTANCE = 40f;
    
    // Behavior probabilities
    public static final float THROW_PROBABILITY_WITH_BALL = 0.4f;
    public static final float PONDER_PROBABILITY_WITHOUT_BALL = 0.3f;
    
    // Drag physics constants
    public static final float ROTATION_SENSITIVITY = 0.08f;
    public static final float ANGULAR_DAMPING = 0.96f;
    public static final float GRAVITY_TORQUE = 15f;
    public static final float MAX_ROTATION = (float) Math.PI / 6; // 30 degrees
    public static final float DROP_FALL_VELOCITY = -100f;
    
    // Particle system constants
    public static final float DUST_VELOCITY_THRESHOLD = 50f;
    public static final int MAX_DUST_PARTICLES = 2;
    public static final float DUST_SPREAD_RADIUS = 15f;
    public static final float DUST_VERTICAL_SPREAD = 8f;
    public static final float DUST_MIN_SPEED = 25f;
    public static final float DUST_MAX_SPEED = 60f;
    
    // Visual constants
    public static final float PILLOW_WIDTH = 50f;
    public static final float PILLOW_HEIGHT = 20f;
    public static final float PILLOW_OFFSET_Y = 5f;
    
    // Text display constants
    public static final float TEXT_OFFSET_X = 5f;
    public static final float TEXT_OFFSET_Y = 15f;
    public static final float CHAR_WIDTH = 12f;
    public static final float CHAR_HEIGHT = 16f;
    public static final float BOLD_THICKNESS = 2f;
    
    // Ball transition constants
    public static final float BALL_TRANSITION_DURATION = 0.3f;
    
    // Private constructor to prevent instantiation
    private GameConstants() {
        throw new UnsupportedOperationException("Constants class should not be instantiated");
    }
}
