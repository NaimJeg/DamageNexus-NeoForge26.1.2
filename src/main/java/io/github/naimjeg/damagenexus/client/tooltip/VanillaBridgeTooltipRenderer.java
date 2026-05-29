package io.github.naimjeg.damagenexus.client.tooltip;

import io.github.naimjeg.damagenexus.util.EnchantmentStackUtil;
import io.github.naimjeg.damagenexus.util.IdentifierText;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;

public final class VanillaBridgeTooltipRenderer {

    private VanillaBridgeTooltipRenderer() {}

    public static boolean hasBridgeEntries(ItemStack stack) {
        return !collectEntries(stack).isEmpty();
    }

    public static void render(
            List<Component> tooltip,
            ItemStack stack,
            boolean detailMode
    ) {
        List<BridgeEntry> entries = collectEntries(stack);

        if (entries.isEmpty()) {
            return;
        }

        for (BridgeEntry entry : entries) {
            tooltip.add(
                    Component.literal("  ")
                            .append(Component.translatable(detailMode
                                    ? "tooltip.damagenexus.marker.vanilla"
                                    : "tooltip.damagenexus.marker.shift"))
                            .append(entry.displayName(detailMode))
                            .withStyle(detailMode
                                    ? ChatFormatting.GRAY
                                    : ChatFormatting.DARK_GRAY)
            );
        }
    }

    public static boolean renderDebug(
            List<Component> tooltip,
            ItemStack stack,
            boolean sectionAlreadyStarted
    ) {
        List<BridgeEntry> entries = collectEntries(stack);

        if (entries.isEmpty()) {
            return sectionAlreadyStarted;
        }

        if (!sectionAlreadyStarted) {
            tooltip.add(
                    Component.translatable("tooltip.damagenexus.debug.header")
                            .withStyle(ChatFormatting.DARK_AQUA)
            );
        }

        for (BridgeEntry entry : entries) {
            tooltip.add(
                    Component.literal("  ")
                            .append(Component.literal(entry.source().toString()))
                            .withStyle(ChatFormatting.DARK_AQUA)
            );

            tooltip.add(debugLine("source", entry.source().toString()));
            tooltip.add(debugLine("level", Integer.toString(entry.level())));
            tooltip.add(debugLine("mode", "VANILLA_ENCHANTMENT_TOOLTIP"));
        }

        return true;
    }

    private static List<BridgeEntry> collectEntries(ItemStack stack) {
        List<BridgeEntry> entries = new ArrayList<>();

        EnchantmentStackUtil.forEachEnchantment(
                stack,
                (ignoredStack, enchantment, level) -> entries.add(new BridgeEntry(
                        sourceId(enchantment),
                        level
                ))
        );

        return List.copyOf(entries);
    }

    private static Identifier sourceId(Holder<Enchantment> enchantment) {
        return enchantment.unwrapKey()
                .map(key -> key.identifier())
                .orElseGet(() -> Identifier.fromNamespaceAndPath(
                        "minecraft",
                        "unknown_enchantment"
                ));
    }

    private static MutableComponent enchantmentName(
            Identifier source,
            boolean includeLevel,
            int level
    ) {
        MutableComponent name = Component.translatable(
                "enchantment."
                        + IdentifierText.namespace(source)
                        + "."
                        + IdentifierText.path(source)
        );

        if (includeLevel) {
            name.append(Component.literal(" "));
            name.append(Component.translatable("enchantment.level." + level));
        }

        return name;
    }

    private static Component debugLine(
            String key,
            String value
    ) {
        return Component.literal("    " + key + "=" + value)
                .withStyle(ChatFormatting.DARK_GRAY);
    }

    private record BridgeEntry(
            Identifier source,
            int level
    ) {
        private MutableComponent displayName(boolean includeLevel) {
            return enchantmentName(source, includeLevel, level);
        }
    }
}
