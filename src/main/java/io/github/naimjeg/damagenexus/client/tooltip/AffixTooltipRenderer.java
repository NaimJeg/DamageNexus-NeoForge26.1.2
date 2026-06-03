package io.github.naimjeg.damagenexus.client.tooltip;

import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
import io.github.naimjeg.damagenexus.api.rule.affix.DamageAffixDefinition;
import io.github.naimjeg.damagenexus.api.rule.affix.DamageAffixDisplay;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class AffixTooltipRenderer {

    private AffixTooltipRenderer() {}

    public static void renderTooltipAffixes(
            List<Component> tooltip,
            List<TooltipAffixView> affixes,
            boolean detailMode
    ) {
        if (tooltip == null || affixes == null || affixes.isEmpty()) {
            return;
        }

        for (TooltipAffixView affix : affixes) {
            if (affix == null) {
                continue;
            }

            appendAffixView(tooltip, affix, detailMode);
        }
    }

    private static void appendAffixView(
            List<Component> tooltip,
            TooltipAffixView affix,
            boolean detailMode
    ) {
        if (affix.displayName() != null
                && !affix.displayName().getString().isBlank()) {
            tooltip.add(
                    affix.displayName()
                            .copy()
                            .withStyle(styleForRarity(affix.rarity().name()))
            );
        }

        for (Component line : affix.tooltipLines()) {
            if (line == null || line.getString().isBlank()) {
                continue;
            }

            tooltip.add(
                    Component.literal("  ")
                            .append(line.copy())
                            .withStyle(ChatFormatting.DARK_GREEN)
            );
        }

        if (detailMode) {
            affix.flavorText()
                    .filter(text -> !text.getString().isBlank())
                    .ifPresent(flavor -> tooltip.add(
                            Component.literal("  ")
                                    .append(flavor.copy())
                                    .withStyle(
                                            ChatFormatting.DARK_GRAY,
                                            ChatFormatting.ITALIC
                                    )
                    ));
        }

        if (affix.showRuleBreakdown() && detailMode) {
            tooltip.add(
                    Component.translatable("tooltip.damagenexus.rules")
                            .withStyle(ChatFormatting.DARK_AQUA)
            );

            for (Identifier ruleId : affix.ruleIds()) {
                tooltip.add(
                        Component.literal("    " + ruleId)
                                .withStyle(ChatFormatting.DARK_AQUA)
                );
            }
        }
    }

    public static void renderItemAffixes(
            List<Component> tooltip,
            List<DamageAffixDefinition> affixes,
            boolean detailMode
    ) {
        renderTooltipAffixes(
                tooltip,
                collectItemAffixViews(affixes),
                detailMode
        );
    }

    public static List<TooltipAffixView> collectItemAffixViews(
            List<DamageAffixDefinition> affixes
    ) {
        if (affixes == null || affixes.isEmpty()) {
            return List.of();
        }

        return affixes.stream()
                .map(AffixTooltipRenderer::toAffixView)
                .toList();
    }

    private static TooltipAffixView toAffixView(
            DamageAffixDefinition affix
    ) {
        DamageAffixDisplay display = affix.display();

        return new TooltipAffixView(
                affix.id(),
                DisplayTextResolver.resolve(display.name()),
                display.tooltip()
                        .stream()
                        .map(DisplayTextResolver::resolve)
                        .toList(),
                display.flavorText().map(DisplayTextResolver::resolve),
                affix.rarity(),
                affix.rules()
                        .stream()
                        .map(DamageRuleDefinition::id)
                        .toList(),
                "",
                display.showRuleBreakdown()
        );
    }

    public static boolean renderDebug(
            List<Component> tooltip,
            List<DamageAffixDefinition> affixes,
            boolean sectionAlreadyStarted
    ) {
        if (tooltip == null || affixes == null || affixes.isEmpty()) {
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

    public static boolean renderTooltipAffixViewDebug(
            List<Component> tooltip,
            List<TooltipAffixView> affixes,
            boolean sectionAlreadyStarted
    ) {
        if (tooltip == null || affixes == null || affixes.isEmpty()) {
            return sectionAlreadyStarted;
        }

        if (!sectionAlreadyStarted) {
            tooltip.add(
                    Component.translatable("tooltip.damagenexus.debug.header")
                            .withStyle(ChatFormatting.DARK_AQUA)
            );
        }

        for (TooltipAffixView affix : affixes) {
            if (affix == null) {
                continue;
            }

            tooltip.add(
                    Component.literal("  ")
                            .append(Component.literal(affix.id().toString()))
                            .withStyle(ChatFormatting.DARK_AQUA)
            );

            tooltip.add(affixViewDebugLine("source", affix.id().toString()));
            tooltip.add(affixViewDebugLine("mode", safeDebugText(affix.debugMode())));
            tooltip.add(affixViewDebugLine("rarity", affix.rarity().name()));

            String displayName = affix.debugDisplayName();
            if (!displayName.isBlank()) {
                tooltip.add(affixViewDebugLine("display", displayName));
            }

            if (affix.ruleIds().isEmpty()) {
                tooltip.add(affixViewDebugLine("rules", "<none>"));
            } else {
                for (Identifier ruleId : affix.ruleIds()) {
                    tooltip.add(affixViewDebugLine("rule", ruleId.toString()));
                }
            }
        }

        return true;
    }

    private static Component affixViewDebugLine(
            String key,
            String value
    ) {
        return Component.literal("    " + key + ": " + value)
                .withStyle(ChatFormatting.DARK_AQUA);
    }

    private static Component debugLine(
            String key,
            String value
    ) {
        return Component.literal("    " + key + "=" + value)
                .withStyle(ChatFormatting.DARK_AQUA);
    }

    private static String safeDebugText(String value) {
        return value == null || value.isBlank()
                ? "<none>"
                : value;
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
}