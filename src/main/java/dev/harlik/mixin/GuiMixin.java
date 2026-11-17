package dev.harlik.mixin;

import dev.harlik.impl.HudLayer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Shadow @Final private GuiRenderState guiRenderState;

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void spell$resetScreenStratum(DeltaTracker deltaTracker, boolean bl, boolean bl2, CallbackInfo ci) {
        HudLayer.screenStratum = -1;
    }

    @Inject(method = "extractRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/Screen;extractRenderStateWithTooltipAndSubtitles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"))
    private void spell$markScreenStratum(DeltaTracker deltaTracker, boolean bl, boolean bl2, CallbackInfo ci) {
        // A screen opens a new stratum before drawing anything, so its first one lands at this index
        HudLayer.screenStratum = ((GuiRenderStateAccessor) guiRenderState).spell$getStrata().size();
    }
}
