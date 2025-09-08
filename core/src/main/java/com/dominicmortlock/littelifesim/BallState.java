package com.dominicmortlock.littelifesim;

public enum BallState {
    FREE,        // Ball is on the ground, can be picked up
    PICKED_UP,   // Ball is being dragged by mouse
    CARRIED,     // Ball is being carried by LittleGuy
    BEING_CAUGHT // Ball is transitioning to being carried
}
