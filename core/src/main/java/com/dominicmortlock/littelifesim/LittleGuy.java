package com.dominicmortlock.littelifesim;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.Random;

/**
 * An autonomous character that can walk around, sleep, carry balls, and be dragged by the player.
 * Features a state machine with behaviors like idle, walking, sleeping, and throwing.
 */
public class LittleGuy extends Entity implements Draggable {
    private State currentState;
    private float stateTimer;
    private float targetX;
    private float targetY;
    private float speed;
    private Random random;
    private TrailEmitter trailEmitter;
    private DraggableComponent draggableComponent;
    private PhysicsComponent physicsComponent;
    private Ball carriedBall; // Ball being carried by this little guy
    private float throwTimer; // Timer for deciding when to throw ball
    private float pickupCooldown; // Cooldown after throwing to prevent immediate pickup
    private TextDisplay textDisplay;
    private Bed currentBed; // Bed currently being used for sleeping
    private float bedCooldown; // Cooldown after leaving bed to prevent immediate re-entry
    
    // Movement and timing constants
    private static final float WALK_SPEED = 50f;
    private static final float MIN_IDLE_TIME = 3f;
    private static final float MAX_IDLE_TIME = 10f;
    private static final float MIN_SLEEP_TIME = 10f;
    private static final float MAX_SLEEP_TIME = 15f;
    private static final float PICKUP_COOLDOWN_TIME = 2f;
    private static final float THROW_ANIMATION_TIME = 0.5f;
    
    // Behavior probabilities
    private static final float THROW_PROBABILITY_WITH_BALL = 0.4f;
    private static final float SLEEP_PROBABILITY_WITHOUT_BALL = 0.2f;
    private static final float COLLISION_MARGIN = 5f;
    private static final float BED_INTERACTION_MARGIN = 10f;
    private static final float BED_SLEEP_TIME = 60f; // 60 seconds in bed
    private static final float BED_COOLDOWN_TIME = 10f; // 10 seconds before can use bed again
    
    
    public LittleGuy(float x, float y) {
        super(x, y, 40, 60, 0f); // Z=0 (middle layer)
        this.currentState = State.IDLE;
        this.stateTimer = 0f;
        this.speed = WALK_SPEED;
        this.random = new Random();
        this.targetX = x;
        this.targetY = y;
        this.trailEmitter = new TrailEmitter(this, 1f, 3f); // 1-3 particles per second
        this.trailEmitter.setActive(false); // Start inactive
        
        // Initialize draggable component and physics
        this.draggableComponent = new DraggableComponent(this);
        this.physicsComponent = new PhysicsComponent(this);
        this.carriedBall = null;
        this.throwTimer = 0f;
        this.pickupCooldown = 0f;
        this.textDisplay = new TextDisplay(this);
        this.currentBed = null;
        this.bedCooldown = 0f;
    }
    
    @Override
    public void update(float deltaTime) {
        stateTimer += deltaTime;
        
        // Update draggable component and physics
        draggableComponent.update(deltaTime);
        physicsComponent.update(deltaTime);
        textDisplay.update(deltaTime);
        
        switch (currentState) {
            case IDLE:
                updateIdleState();
                break;
            case WALKING:
                updateWalkingState(deltaTime);
                break;
            case PICKED_UP:
                updatePickedUpState(deltaTime);
                break;
            case THROWING:
                updateThrowingState(deltaTime);
                break;
            case SLEEPING:
                updateSleepingState();
                break;
            case SLEEPING_IN_BED:
                updateSleepingInBedState();
                break;
            case CARRIED_BY_BED:
                updateCarriedByBedState();
                break;
        }
        
        // Update cooldowns
        if (pickupCooldown > 0f) {
            pickupCooldown -= deltaTime;
        }
        if (bedCooldown > 0f) {
            bedCooldown -= deltaTime;
        }
        
        // Check for ball pickup when not being dragged and cooldown expired
        // Check more frequently for better collision detection
        if (currentState != State.PICKED_UP && currentState != State.THROWING && 
            currentState != State.SLEEPING && currentState != State.SLEEPING_IN_BED && 
            currentState != State.CARRIED_BY_BED && carriedBall == null && pickupCooldown <= 0f) {
            checkForBallPickup();
        }
        
        // Check for bed interaction when idle or walking (with cooldown)
        if ((currentState == State.IDLE || currentState == State.WALKING) && 
            currentBed == null && bedCooldown <= 0f) {
            checkForBedInteraction();
        }
    }
    
    private void updateIdleState() {
        float idleTime = getRandomTime(MIN_IDLE_TIME, MAX_IDLE_TIME);
        if (stateTimer >= idleTime) {
            decideNextAction();
        }
    }
    
    private void decideNextAction() {
        if (hasValidCarriedBall()) {
            // With ball: throw or walk
            if (random.nextFloat() < THROW_PROBABILITY_WITH_BALL) {
                startThrowing();
            } else {
                startWalking();
            }
        } else {
            // Clear stale ball reference
            carriedBall = null;
            
            // Without ball: sleep or walk
            if (random.nextFloat() < SLEEP_PROBABILITY_WITHOUT_BALL) {
                startSleeping();
            } else {
                startWalking();
            }
        }
    }
    
    private boolean hasValidCarriedBall() {
        return carriedBall != null && carriedBall.getCurrentState() == BallState.CARRIED;
    }
    
    private float getRandomTime(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }
    
    private void checkForBedInteraction() {
        if (map == null) return;
        
        // Check all entities in the map for beds
        for (Entity entity : map.getEntities()) {
            if (entity instanceof Bed) {
                Bed bed = (Bed) entity;
                if (!bed.isOccupied() && bed.isNearby(this, BED_INTERACTION_MARGIN)) {
                    startSleepingInBed(bed);
                    break; // Only use one bed at a time
                }
            }
        }
    }
    
    private void startSleepingInBed(Bed bed) {
        currentState = State.SLEEPING_IN_BED;
        stateTimer = 0f;
        currentBed = bed;
        
        // Move to sleeping position in bed
        float[] sleepingPos = bed.getSleepingPosition(this);
        setPosition(sleepingPos[0], sleepingPos[1]);
        
        // Occupy the bed
        bed.setOccupied(true, this);
        
        // Drop any carried ball
        if (carriedBall != null) {
            dropBall();
        }
        
        // Show sleeping mood for the full duration
        textDisplay.setMood("zzz", 1.0f, BED_SLEEP_TIME);
        
        // Deactivate trail particles
        trailEmitter.setActive(false);
    }
    
    public void wakeUpFromBed() {
        if (currentBed != null) {
            currentBed.setOccupied(false, null);
            currentBed = null;
        }
        bedCooldown = BED_COOLDOWN_TIME; // Start cooldown
        startIdling();
    }
    
    private void updateCarriedByBedState() {
        // If bed stops being dragged or moves away, exit this state
        if (currentBed == null || !currentBed.getDraggableComponent().isBeingDragged()) {
            // Exit carried state
            if (currentBed != null) {
                currentBed.setOccupied(false, null);
                currentBed = null;
            }
            bedCooldown = BED_COOLDOWN_TIME; // Start cooldown
            startIdling();
        } else {
            // Stay positioned on the bed
            float[] bedPosition = currentBed.getSleepingPosition(this);
            setPosition(bedPosition[0], bedPosition[1]);
        }
    }
    
    public void startBeingCarriedByBed(Bed bed) {
        currentState = State.CARRIED_BY_BED;
        stateTimer = 0f;
        currentBed = bed;
        
        // Drop any carried ball
        if (carriedBall != null) {
            dropBall();
        }
        
        // Show surprised mood
        textDisplay.setMood("!", 0.9f, 2f);
        
        // Deactivate trail particles
        trailEmitter.setActive(false);
    }
    
    public Bed getCurrentBed() {
        return currentBed;
    }
    
    public State getCurrentState() {
        return currentState;
    }
    
    private void updateWalkingState(float deltaTime) {
        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distance < 5f) { // Close enough to target
            startIdling();
        } else {
            // Move towards target
            float moveX = (dx / distance) * speed * deltaTime;
            float moveY = (dy / distance) * speed * deltaTime;
            setPosition(x + moveX, y + moveY);
        }
    }
    
    private void startWalking() {
        currentState = State.WALKING;
        stateTimer = 0f;
        
        // Set walking mood (low chance to show)
        textDisplay.setMood("...", 0.1f, 1.5f); // 10% chance when walking, 1.5s duration
        
        // Activate trail particles
        trailEmitter.setActive(true);
        
        // Pick a random target within map bounds
        if (map != null) {
            targetX = random.nextFloat() * (map.getWidth() - width);
            targetY = random.nextFloat() * (map.getHeight() - height);
        }
    }
    
    private void startIdling() {
        currentState = State.IDLE;
        stateTimer = 0f;
        
        // Set mood based on context (low chance for idle)
        if (carriedBall != null) {
            textDisplay.setMood("hmm", 0.1f, 2f); // 10% chance when idle with ball, 2s duration
        } else {
            textDisplay.setMood("zzz", 0.05f, 2f); // 5% chance when idle without ball, 2s duration
        }
        
        // Deactivate trail particles
        trailEmitter.setActive(false);
    }
    
    private void updatePickedUpState(float deltaTime) {
        // Update drag physics through draggable component
        draggableComponent.updateDrag(deltaTime);
    }
    
    private void updateThrowingState(float deltaTime) {
        // Wait for throw animation to complete
        if (stateTimer >= THROW_ANIMATION_TIME) {
            // Throw completed, go to idle
            startIdling();
        }
    }
    
    private void updateSleepingState() {
        float sleepTime = getRandomTime(MIN_SLEEP_TIME, MAX_SLEEP_TIME);
        if (stateTimer >= sleepTime) {
            startIdling();
        }
    }
    
    private void updateSleepingInBedState() {
        if (stateTimer >= BED_SLEEP_TIME) {
            // Wake up and leave bed
            wakeUpFromBed();
        }
    }
    
    private void startSleeping() {
        currentState = State.SLEEPING;
        stateTimer = 0f;
        
        // Always show zzz when sleeping (100% chance) - match sleep duration
        float sleepDuration = getRandomTime(MIN_SLEEP_TIME, MAX_SLEEP_TIME);
        textDisplay.setMood("zzz", 1.0f, sleepDuration);
        
        // Deactivate trail particles
        trailEmitter.setActive(false);
    }
    
    private void checkForBallPickup() {
        if (map == null) return;
        
        // Check all entities in the map for balls
        for (Entity entity : map.getEntities()) {
            if (entity instanceof Ball) {
                Ball ball = (Ball) entity;
                if (ball.isFree() && isIntersecting(ball)) {
                    pickupBall(ball);
                    break; // Only pick up one ball at a time
                }
            }
        }
    }
    
    private boolean isIntersecting(Entity other) {
        // More generous collision detection for better ball catching
        return x - COLLISION_MARGIN < other.getX() + other.getWidth() &&
               x + width + COLLISION_MARGIN > other.getX() &&
               y - COLLISION_MARGIN < other.getY() + other.getHeight() &&
               y + height + COLLISION_MARGIN > other.getY();
    }
    
    private void pickupBall(Ball ball) {
        carriedBall = ball;
        ball.startCarried(this);
        textDisplay.setMood("!", 0.8f, 1.5f); // 80% chance when catching ball, 1.5s duration
    }
    
    public void dropBall() {
        if (carriedBall != null) {
            carriedBall.dropWithArc(); // Use new arc drop method
            carriedBall = null;
            throwTimer = 0f;
        }
    }
    
    private void startThrowing() {
        if (carriedBall == null) {
            // Safety fallback - if no ball, just start walking instead
            startWalking();
            return;
        }
        
        currentState = State.THROWING;
        stateTimer = 0f;
        
        // Set throwing mood (high chance to show)
        textDisplay.setMood("!", 0.9f, 1f); // 90% chance when throwing, 1s duration
        
        // Perform the actual throw
        throwBall();
    }
    
    private void throwBall() {
        if (carriedBall == null) return;
        
        // Calculate throw direction (somewhat random, but generally forward)
        float throwDirection = (random.nextFloat() - 0.5f) * 2f; // -1 to 1
        float throwVelocityX = 150f + random.nextFloat() * 100f; // 150-250 pixels/sec
        if (throwDirection < 0) throwVelocityX = -throwVelocityX; // Sometimes throw backwards
        
        float throwVelocityY = 120f + random.nextFloat() * 80f; // 120-200 pixels/sec upward
        
        carriedBall.throwBall(throwVelocityX, throwVelocityY);
        carriedBall = null;
        throwTimer = 0f;
        
        // Start pickup cooldown
        pickupCooldown = PICKUP_COOLDOWN_TIME;
    }
    
    // Draggable interface implementation
    @Override
    public DraggableComponent getDraggableComponent() {
        return draggableComponent;
    }
    
    @Override
    public void onDragStart() {
        // If sleeping in bed, wake up and leave bed
        if (currentState == State.SLEEPING_IN_BED) {
            wakeUpFromBed();
        }
        // If being carried by bed, release from bed
        else if (currentState == State.CARRIED_BY_BED) {
            if (currentBed != null) {
                currentBed.setOccupied(false, null);
                currentBed = null;
            }
            bedCooldown = BED_COOLDOWN_TIME;
        }
        
        currentState = State.PICKED_UP;
        stateTimer = 0f;
        trailEmitter.setActive(false);
        
        // Drop any carried ball when picked up
        dropBall();
        
        draggableComponent.startDrag();
        textDisplay.setMood("?", 0.7f, 1f); // 70% chance when picked up, 1s duration
    }
    
    @Override
    public void onDragStop() {
        // Get throw velocity from draggable component (very reduced power for LittleGuy)
        float throwVelocityX = draggableComponent.getDragVelocityX() * 0.03f; // 90% less than before
        float throwVelocityY = draggableComponent.getDragVelocityY() * 0.03f;
        
        // Ensure minimum upward velocity for throwing
        if (Math.abs(throwVelocityX) > 20f || Math.abs(throwVelocityY) > 20f) {
            throwVelocityY = Math.max(throwVelocityY, 60f); // Minimum upward throw
            physicsComponent.launch(throwVelocityX, throwVelocityY);
        }
        
        draggableComponent.stopDrag();
        startIdling();
        
        // Drop any carried ball when we get thrown
        if (carriedBall != null) {
            dropBall();
        }
    }
    
    public boolean isPointInside(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && 
               mouseY >= y && mouseY <= y + height;
    }
    
    public boolean isPickedUp() {
        return currentState == State.PICKED_UP;
    }
    
    @Override
    public void setMap(Map map) {
        super.setMap(map);
        if (map != null && trailEmitter != null) {
            map.getParticleSystem().addEmitter(trailEmitter);
        }
        if (physicsComponent != null) {
            physicsComponent.setMap(map);
        }
    }
    
    @Override
    public void render(ShapeRenderer shapeRenderer) {
        // Change color based on state
        if (currentState == State.PICKED_UP) {
            shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f); // Light gray when picked up
        } else if (currentState == State.SLEEPING_IN_BED) {
            shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 1f); // Very dark when sleeping in bed
        } else if (currentState == State.CARRIED_BY_BED) {
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f); // Dark gray when carried by bed
        } else {
            shapeRenderer.setColor(0f, 0f, 0f, 1f); // Black for all other states
        }
        
        // Draw rotated rectangle around grab point (80% height)
        float pivotX = x + width / 2; // Center X
        float pivotY = y + height * 0.8f; // 80% height (grab point)
        
        RenderUtils.renderRotatedRectangle(shapeRenderer, x, y, width, height, 
                                         draggableComponent.getRotation(), pivotX, pivotY);
        
        // Render text display
        textDisplay.render(shapeRenderer);
    }
}
