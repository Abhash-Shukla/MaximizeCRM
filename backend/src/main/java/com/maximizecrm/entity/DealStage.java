package com.maximizecrm.entity;

public enum DealStage {
    PROSPECT,
    NEGOTIATION,
    WON,
    LOST;

    /**
     * Enforces forward-only progression through deal stages:
     * - PROSPECT can transition to PROSPECT, NEGOTIATION, WON, LOST.
     * - NEGOTIATION can transition to NEGOTIATION, WON, LOST (cannot go back to PROSPECT).
     * - WON and LOST are closed/terminal stages and cannot transition to other stages.
     */
    public boolean canTransitionTo(DealStage targetStage) {
        if (targetStage == null) {
            return false;
        }
        if (this == targetStage) {
            return true;
        }
        return switch (this) {
            case PROSPECT -> targetStage == NEGOTIATION || targetStage == WON || targetStage == LOST;
            case NEGOTIATION -> targetStage == WON || targetStage == LOST;
            case WON, LOST -> false; // Terminal stages: cannot transition to any other stage
        };
    }

    public boolean isClosed() {
        return this == WON || this == LOST;
    }
}

