package dev.isxander.adaptivetooltips.mixins;

import dev.isxander.adaptivetooltips.config.AdaptiveTooltipConfig;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * NeoForge patches {@link TooltipRenderUtil}: {@code GuiGraphics} calls the 9-int overload and
 * passes the four colors from {@code RenderTooltipEvent.Color}; the old 6-arg overload with
 * hard-coded constants is not used by vanilla rendering anymore. Apply transparency to the
 * color parameters of that overload instead.
 */
@Mixin(TooltipRenderUtil.class)
public class TooltipRenderUtilMixin {
    private static final String RENDER_BACKGROUND =
            "renderTooltipBackground(Lnet/minecraft/client/gui/GuiGraphics;IIIIIIIII)V";

    @ModifyVariable(method = RENDER_BACKGROUND, at = @At("HEAD"), argsOnly = true, index = 6)
    private static int adaptive$backgroundStart(int color) {
        return applyTransparency(color);
    }

    @ModifyVariable(method = RENDER_BACKGROUND, at = @At("HEAD"), argsOnly = true, index = 7)
    private static int adaptive$backgroundEnd(int color) {
        return applyTransparency(color);
    }

    @ModifyVariable(method = RENDER_BACKGROUND, at = @At("HEAD"), argsOnly = true, index = 8)
    private static int adaptive$borderStart(int color) {
        return applyTransparency(color);
    }

    @ModifyVariable(method = RENDER_BACKGROUND, at = @At("HEAD"), argsOnly = true, index = 9)
    private static int adaptive$borderEnd(int color) {
        return applyTransparency(color);
    }

    private static int applyTransparency(int argb) {
        float transparency = AdaptiveTooltipConfig.HANDLER.instance().tooltipTransparency;
        if (transparency >= 1.0F)
            return argb;

        int alpha = (argb >>> 24) & 0xFF;
        int newAlpha = Math.max(0, Math.min(255, (int) (alpha * transparency)));
        return (argb & 0x00FFFFFF) | (newAlpha << 24);
    }
}
