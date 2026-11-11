package dev.isxander.adaptivetooltips.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.adaptivetooltips.config.AdaptiveTooltipConfig;
import dev.isxander.adaptivetooltips.helpers.ScrollTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MouseMixin {
    @WrapOperation(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseScrolled(DDDD)Z"))
    private boolean trackMouseWheel(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount, Operation<Boolean> original) {
        long window = Minecraft.getInstance().getWindow().getWindow();
        if (InputConstants.isKeyDown(window, AdaptiveTooltipConfig.HANDLER.instance().scrollKeyCode)) {
            if (InputConstants.isKeyDown(window, AdaptiveTooltipConfig.HANDLER.instance().horizontalScrollKeyCode)) {
                ScrollTracker.addHorizontalScroll((int) Math.signum(verticalAmount));
            } else {
                ScrollTracker.addVerticalScroll((int) Math.signum(verticalAmount));
                ScrollTracker.addHorizontalScroll((int) Math.signum(horizontalAmount));
            }
            return false;
        }

        return original.call(screen, mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
