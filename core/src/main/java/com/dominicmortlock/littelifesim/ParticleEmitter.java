package com.dominicmortlock.littelifesim;

public interface ParticleEmitter {
    void update(float deltaTime, ParticleSystem particleSystem);
    boolean isActive();
    void setActive(boolean active);
}
