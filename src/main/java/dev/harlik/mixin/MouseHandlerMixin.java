package dev.harlik.mixin;

import dev.harlik.impl.drag.DragManager;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void spell$onPress(long window, MouseButtonInfo info, int action, CallbackInfo ci) {
        if (DragManager.INSTANCE.onPress(info.button(), action)) ci.cancel();
    }
}
