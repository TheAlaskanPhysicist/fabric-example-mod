package com.stanieldev.relativity.config;

public class RelativityConfig {

    // Entity History
    public static int MAX_SNAPSHOT_COUNT = 200;
    public static final long TEMP_MAX_TIME_RETARDATION = 40; // Ticks
    public static int PRUNE_FREQUENCY = 100;  // How often (in ticks) to prune removed entities
    public static float SPEED_OF_LIGHT = 0.2F; // Blocks/tick
}
