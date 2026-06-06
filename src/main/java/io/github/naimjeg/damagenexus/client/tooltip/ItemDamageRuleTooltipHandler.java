package io.github.naimjeg.damagenexus.client.tooltip;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.rule.affix.DamageAffixDefinition;
import io.github.naimjeg.damagenexus.api.rule.affix.DamageAffixSelectionResolver;
import io.github.naimjeg.damagenexus.api.rule.entry.DamageEntryDefinition;
import io.github.naimjeg.damagenexus.api.rule.entry.DamageEntrySelectionResolver;
import io.github.naimjeg.damagenexus.config.DamageNexusConfig;
import io.github.naimjeg.damagenexus.registry.ModDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(
        modid = DamageNexus.MODID,
        value = Dist.CLIENT
)
final class ItemDamageRuleTooltipHandler {

    private ItemDamageRuleTooltipHandler() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        DamageNexusClientTooltips.register();

        ItemStack stack = event.getItemStack();

        List<DamageEntryDefinition> entries =
                stack.getOrDefault(
                        ModDataComponents.DAMAGE_ENTRIES.get(),
                        List.of()
                );

        List<DamageAffixDefinition> affixes =
                stack.getOrDefault(
                        ModDataComponents.DAMAGE_AFFIXES.get(),
                        List.of()
                );

        List<DamageEntryDefinition> selectedEntries =
                DamageEntrySelectionResolver.resolve(entries);

        List<DamageAffixDefinition> selectedAffixes =
                DamageAffixSelectionResolver.resolve(affixes);

        List<DamageTooltipView> vanillaEnchantmentViews =
                VanillaEnchantmentTooltipAdapter.collectTooltipViews(stack);

        List<DamageTooltipView> tooltipViews = new ArrayList<>();
        tooltipViews.addAll(DamageEntryTooltipAdapter.collectItemEntryViews(
                selectedEntries
        ));
        tooltipViews.addAll(DamageTooltipRenderer.collectItemAffixViews(
                selectedAffixes
        ));
        tooltipViews.addAll(vanillaEnchantmentViews);

        if (tooltipViews.isEmpty()) {
            return;
        }

        boolean detailMode = event.getFlags().hasShiftDown() || isShiftDown();
        boolean debugMode =
                DamageNexusConfig.current().tooltips().debugTooltipsEnabled();

        List<Component> tooltip = event.getToolTip();

        DamageTooltipRenderer.renderTooltipViews(
                tooltip,
                tooltipViews,
                detailMode
        );

        if (debugMode) {
            boolean debugSectionStarted = DamageTooltipRenderer.renderDebug(
                    tooltip,
                    selectedAffixes,
                    false
            );

            DamageEntryTooltipAdapter.renderDebug(
                    tooltip,
                    selectedEntries,
                    debugSectionStarted
            );

            DamageTooltipRenderer.renderTooltipViewDebug(
                    tooltip,
                    vanillaEnchantmentViews,
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
