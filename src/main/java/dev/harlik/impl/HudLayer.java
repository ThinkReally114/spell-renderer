package dev.harlik.impl;

/**
 * Where the HUD context sits in vanilla's GUI. Vanilla draws the whole GUI in one go, split in two at most:
 * everything before the menu blur, then everything after it. The HUD context draws right at that split,
 * above the vanilla HUD and below the open screen.
 */
public final class HudLayer {

    /** Index of the open screen's first stratum in this frame's GUI render state, or -1 when no screen is open. */
    public static int screenStratum = -1;

    private HudLayer() {}
}
