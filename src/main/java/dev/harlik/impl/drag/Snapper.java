package dev.harlik.impl.drag;

import dev.harlik.api.scissors.Region;

@FunctionalInterface
public interface Snapper {
    /** Called every frame while dragging with where the shape would be if it followed the mouse.
     *  Return where it should go instead; only x and y are used, null means don't snap.
     *  Don't draw or submit shapes here, do it in a render listener. */
    Region snap(String id, Region proposed);
}
