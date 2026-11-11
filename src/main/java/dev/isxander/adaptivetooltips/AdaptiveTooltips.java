package dev.isxander.adaptivetooltips;

import dev.isxander.adaptivetooltips.config.AdaptiveTooltipConfig;
import dev.isxander.adaptivetooltips.config.gui.KeyCode;
import dev.isxander.yacl3.config.v2.api.autogen.OptionFactory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = AdaptiveTooltips.MOD_ID, dist = Dist.CLIENT)
public class AdaptiveTooltips {
    public static final String MOD_ID = "adaptivetooltips";

    public AdaptiveTooltips(IEventBus modEventBus, ModContainer container) {
        AdaptiveTooltipConfig.HANDLER.load();
        AdaptiveTooltipConfig.HANDLER.instance().refreshKeyHints();
        OptionFactory.register(KeyCode.class, new KeyCode.KeyCodeImpl());

        container.registerExtensionPoint(IConfigScreenFactory.class,
                (modContainer, parent) -> {
                    AdaptiveTooltipConfig.HANDLER.instance().refreshKeyHints();
                    return AdaptiveTooltipConfig.HANDLER.generateGui().generateScreen(parent);
                });
    }
}
