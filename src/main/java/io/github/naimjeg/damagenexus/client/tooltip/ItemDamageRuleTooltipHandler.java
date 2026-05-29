package io.github.naimjeg.damagenexus.client.tooltip;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
import io.github.naimjeg.damagenexus.registry.ModDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@EventBusSubscriber(
        modid = DamageNexus.MODID,
        value = Dist.CLIENT
)
public final class ItemDamageRuleTooltipHandler {

    private ItemDamageRuleTooltipHandler() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        DamageNexusClientTooltips.register();

        ItemStack stack = event.getItemStack();

        List<DamageRuleDefinition> rules =
                stack.getOrDefault(
                        ModDataComponents.DAMAGE_RULES.get(),
                        List.of()
                );

        boolean hasVanillaBridgeTooltips =
                VanillaBridgeTooltipRenderer.hasBridgeEntries(stack);

        if (rules.isEmpty() && !hasVanillaBridgeTooltips) {
            return;
        }

        boolean detailMode = event.getFlags().hasShiftDown() || isShiftDown();
        boolean debugMode = event.getFlags().isAdvanced() || ModConfig.isDebugMode();

        List<Component> tooltip = event.getToolTip();

        RuleTooltipRenderer.renderItemRules(
                tooltip,
                rules,
                detailMode
        );

        VanillaBridgeTooltipRenderer.render(
                tooltip,
                stack,
                detailMode
        );

        if (debugMode) {
            boolean debugSectionStarted = RuleTooltipRenderer.renderDebug(
                    tooltip,
                    rules,
                    false
            );

            VanillaBridgeTooltipRenderer.renderDebug(
                    tooltip,
                    stack,
                    debugSectionStarted
            );
        }
    }

    private static boolean isShiftDown() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft == null || minecraft.getWindow() == null) {
            return false;
        }

        Window window = minecraft.getWindow();

        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }
}
