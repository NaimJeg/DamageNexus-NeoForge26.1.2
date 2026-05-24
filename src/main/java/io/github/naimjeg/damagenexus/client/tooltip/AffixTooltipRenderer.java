package io.github.naimjeg.damagenexus.client.tooltip;

import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
import io.github.naimjeg.damagenexus.api.rule.affix.DamageAffixDefinition;
import io.github.naimjeg.damagenexus.api.rule.affix.DamageAffixDisplay;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class AffixTooltipRenderer {

    private AffixTooltipRenderer() {}

    public static void renderItemAffixes(
            List<Component> tooltip,
            List<DamageAffixDefinition> affixes,
            boolean detailMode
    ) {
        if (affixes.isEmpty()) {
            return;
        }

        for (DamageAffixDefinition affix : affixes) {
            appendAffix(
                    tooltip,
                    affix,
                    detailMode
            );
        }
    }

    public static boolean renderDebug(
            List<Component> tooltip,
            List<DamageAffixDefinition> affixes,
            boolean sectionAlreadyStarted
    ) {
        if (affixes.isEmpty()) {
            return sectionAlreadyStarted;
        }

        if (!sectionAlreadyStarted) {
            tooltip.add(
                    Component.translatable("tooltip.damagenexus.debug.header")
                            .withStyle(ChatFormatting.DARK_AQUA)
            );
        }

        for (DamageAffixDefinition affix : affixes) {
            tooltip.add(
                    Component.literal("  Affix: ")
                            .append(Component.literal(affix.id().toString()))
                            .withStyle(ChatFormatting.DARK_AQUA)
            );

            tooltip.add(debugLine("rarity", affix.rarity().name()));
            tooltip.add(debugLine("slot", affix.slot().name()));
            tooltip.add(debugLine("stacking", affix.stacking().name()));

            affix.stackingGroup()
                    .ifPresent(group -> tooltip.add(debugLine(
                            "stacking_group",
                            group.toString()
                    )));

            for (DamageRuleDefinition rule : affix.rules()) {
                tooltip.add(
                        Component.literal("    rule=")
                                .append(Component.literal(rule.id().toString()))
                                .withStyle(ChatFormatting.DARK_AQUA)
                );
                tooltip.add(debugLine("    phase", rule.phase().name()));
                tooltip.add(debugLine("    role", rule.role().name()));
                tooltip.add(debugLine("    priority", Integer.toString(rule.priority())));
            }
        }

        return true;
    }

    private static void appendAffix(
            List<Component> tooltip,
            DamageAffixDefinition affix,
            boolean detailMode
    ) {
        DamageAffixDisplay display = affix.display();

        if (!display.name().isBlank()) {
            tooltip.add(
                    Component.literal(display.name())
                            .withStyle(styleForRarity(affix.rarity().name()))
            );
        }

        for (String line : display.tooltip()) {
            if (line == null || line.isBlank()) {
                continue;
            }

            tooltip.add(
                    Component.literal("  ")
                            .append(Component.literal(line))
                            .withStyle(ChatFormatting.DARK_GREEN)
            );
        }

        if (detailMode) {
            display.flavorText()
                    .filter(text -> !text.isBlank())
                    .ifPresent(flavor -> tooltip.add(
                            Component.literal("  ")
                                    .append(Component.literal(flavor))
                                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                    ));
        }

        if (display.showRuleBreakdown() && detailMode) {
            tooltip.add(
                    Component.literal("  Rules:")
                            .withStyle(ChatFormatting.DARK_AQUA)
            );

            for (DamageRuleDefinition rule : affix.rules()) {
                tooltip.add(
                        Component.literal("    " + rule.id())
                                .withStyle(ChatFormatting.DARK_AQUA)
                );
            }
        }
    }

    private static ChatFormatting styleForRarity(String rarity) {
        return switch (rarity) {
            case "UNCOMMON" -> ChatFormatting.GREEN;
            case "RARE" -> ChatFormatting.BLUE;
            case "EPIC" -> ChatFormatting.DARK_PURPLE;
            case "LEGENDARY" -> ChatFormatting.GOLD;
            case "UNIQUE" -> ChatFormatting.LIGHT_PURPLE;
            default -> ChatFormatting.GRAY;
        };
    }

    private static Component debugLine(String key, String value) {
        return Component.literal("    " + key + "=" + value)
                .withStyle(ChatFormatting.DARK_AQUA);
    }
}