package com.justeat.model;

public enum OrderStatus {
    PENDING,
    PREPARING,
    READY,
    COMPLETED;

    /**
     * Enforces US 3.5: orders can only move forward through the workflow,
     * never backwards (e.g. READY -> PREPARING is rejected).
     */
    public boolean canTransitionTo(OrderStatus next) {
        return next.ordinal() == this.ordinal() + 1;
    }
}
