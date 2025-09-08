package com.dominicmortlock.littelifesim;

public interface Draggable {
    DraggableComponent getDraggableComponent();
    boolean isPointInside(float mouseX, float mouseY);
    void onDragStart();
    void onDragStop();
}
