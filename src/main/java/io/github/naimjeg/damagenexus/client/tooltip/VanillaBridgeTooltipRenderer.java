package io.github.naimjeg.damagenexus.client.tooltip;

import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleOperation;
import io.github.naimjeg.damagenexus.api.rule.affix.DamageAffixRarity;
import io.github.naimjeg.damagenexus.util.EnchantmentStackUtil;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class VanillaBridgeTooltipRenderer {

    private VanillaBridgeTooltipRenderer() {}

    public static boolean hasBridgeEntries(ItemStack stack) {
        return !collectEntries(stack).isEmpty();
    }

    public static List<TooltipAffixView> collectAffixViews(ItemStack stack) {
        return collectEntries(stack)
                .stream()
                .map(VanillaBridgeTooltipRenderer::toAffixView)
                .flatMap(Optional::stream)
                .toList();
    }

    private static Optional<TooltipAffixView> toAffixView(
            BridgeEntry entry
    ) {
        return VanillaBridgeTooltipCatalog
                .create(entry.source(), entry.level())
                .map(spec -> new TooltipAffixView(
                        spec.source(),
                        spec.displayName(),
                        bridgeTooltipLines(spec),
                        Optional.empty(),
                        DamageAffixRarity.COMMON,
                        List.of(spec.source()),
                        "VANILLA_ENCHANTMENT_TOOLTIP",
                        false
                ));
    }

    private static List<BridgeEntry> collectEntries(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return List.of();
        }

        List<BridgeEntry> entries = new ArrayList<>();

        EnchantmentStackUtil.forEachEnchantment(
                stack,
                (ignoredStack, enchantment, level) -> {
                    if (level <= 0) {
                        return;
                    }

                    entries.add(new BridgeEntry(
                            sourceId(enchantment),
                            level
                    ));
                }
        );

        return List.copyOf(entries);
    }

    private static Identifier sourceId(
            Holder<Enchantment> enchantment
    ) {
        if (enchantment == null) {
            return unknownEnchantmentId();
        }

        return enchantment.unwrapKey()
                .map(key -> key.identifier())
                .orElseGet(VanillaBridgeTooltipRenderer::unknownEnchantmentId);
    }

    private static Identifier unknownEnchantmentId() {
        return Identifier.fromNamespaceAndPath(
                "minecraft",
                "unknown_enchantment"
        );
    }

    private static List<Component> bridgeTooltipLines(
            VanillaBridgeTooltipSpec spec
    ) {
        return spec.operations()
                .stream()
                .map(operation -> bridgeOperationLine(
                        operation,
                        spec.conditions()
                ))
                .map(component -> (Component) component)
                .toList();
    }

    private static MutableComponent bridgeOperationLine(
            DamageRuleOperation operation,
            List<DamageRuleCondition> conditions
    ) {
        MutableComponent line = Component.empty()
                .append(RuleTooltipDescriptions.describeOperation(
                        operation,
                        RuleTooltipMode.NORMAL
                ));

        if (conditions == null || conditions.isEmpty()) {
            return line;
        }

        return line.append(Component.literal(" "))
                .append(Component.translatable(
                        "tooltip.damagenexus.condition_suffix",
                        joinConditions(conditions)
                ));
    }

    private static MutableComponent joinConditions(
            List<DamageRuleCondition> conditions
    ) {
        MutableComponent result = Component.empty();

        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) {
                result.append(Component.translatable(
                        "tooltip.damagenexus.separator.comma"
                ));
            }

            result.append(RuleTooltipDescriptions.describeCondition(
                    conditions.get(i),
                    RuleTooltipMode.NORMAL
            ));
        }

        return result;
    }

    private record BridgeEntry(
            Identifier source,
            int level
    ) {}
}