package com.stanieldev.relativity.client.render;

public class RelativityModelContext {

    public static class ModelAnimState {
        public final float limbPos;
        public final float limbSpeed;

        public ModelAnimState(float limbPos, float limbSpeed) {
            this.limbPos = limbPos;
            this.limbSpeed = limbSpeed;
        }
    }

    private static final ThreadLocal<ModelAnimState> CURRENT_STATE = new ThreadLocal<>();

    public static void set(float limbPos, float limbSpeed) {
        CURRENT_STATE.set(new ModelAnimState(limbPos, limbSpeed));
    }

    public static ModelAnimState get() {
        return CURRENT_STATE.get();
    }

    public static void clear() {
        CURRENT_STATE.remove();
    }
}