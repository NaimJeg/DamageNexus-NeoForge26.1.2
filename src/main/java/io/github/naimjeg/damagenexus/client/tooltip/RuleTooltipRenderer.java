package io.github.naimjeg.damagenexus.client.tooltip;

import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleOperation;
import io.github.naimjeg.damagenexus.builtin.rule.condition.AlwaysCondition;
import io.github.naimjeg.damagenexus.util.IdentifierText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RuleTooltipRenderer {

    private RuleTooltipRenderer() {}

    public static void renderItemRules(
            List<Component> tooltip,
            List<DamageRuleDefinition> rules,
            boolean detailMode
    ) {
        /*
         * Tooltip rendering is schema-only.
         * Do not evaluate runtime conditions or execute operations here.
         */
        if (rules.isEmpty()) {
            return;
        }

        List<DamageRuleDefinition> normalRules = new ArrayList<>();
        List<DamageRuleDefinition> conditionalRules = new ArrayList<>();

        for (DamageRuleDefinition rule : rules) {
            if (isUnconditionalRule(rule)) {
                normalRules.add(rule);
            } else {
                conditionalRules.add(rule);
            }
        }

        appendNormalRules(tooltip, normalRules, detailMode);

        Map<Identifier, List<DamageRuleDefinition>> conditionalGroups =
                groupRules(conditionalRules);

        if (conditionalGroups.isEmpty()) {
            return;
        }

        if (!detailMode) {
            appendCollapsedConditionalGroups(tooltip, conditionalGroups);
            return;
        }

        appendExpandedConditionalGroups(tooltip, conditionalGroups);
    }

    private static void appendNormalRules(
            List<Component> tooltip,
            List<DamageRuleDefinition> rules,
            boolean detailMode
    ) {
        RuleTooltipMode mode =
                detailMode ? RuleTooltipMode.DETAIL : RuleTooltipMode.NORMAL;

        for (DamageRuleDefinition rule : rules) {
            if (detailMode) {
                appendRuleDisplay(
                        tooltip,
                        rule,
                        "",
                        true
                );
            }

            appendRuleOperations(
                    tooltip,
                    rule,
                    mode,
                    ""
            );
        }
    }

    private static void appendCollapsedConditionalGroups(
            List<Component> tooltip,
            Map<Identifier, List<DamageRuleDefinition>> groups
    ) {
        for (Map.Entry<Identifier, List<DamageRuleDefinition>> group : groups.entrySet()) {
            tooltip.add(
                    Component.literal("[Shift] ")
                            .append(formatRuleGroupName(
                                    group.getKey(),
                                    group.getValue()
                            ))
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
    }

    private static void appendExpandedConditionalGroups(
            List<Component> tooltip,
            Map<Identifier, List<DamageRuleDefinition>> groups
    ) {

        for (Map.Entry<Identifier, List<DamageRuleDefinition>> group : groups.entrySet()) {
            List<DamageRuleDefinition> rules = group.getValue();

            tooltip.add(
                    formatRuleGroupName(group.getKey(), rules)
                            .withStyle(ChatFormatting.AQUA)
            );

            List<DamageRuleCondition> conditions = collectConditions(rules);

            for (DamageRuleCondition condition : conditions) {
                tooltip.add(
                        Component.literal("  ")
                                .append(RuleTooltipDescriptions.describeCondition(
                                        condition,
                                        RuleTooltipMode.DETAIL
                                ))
                                .withStyle(ChatFormatting.GRAY)
                );
            }

            for (DamageRuleDefinition rule : rules) {
                appendRuleDisplay(
                        tooltip,
                        rule,
                        "  ",
                        false
                );

                appendRuleOperations(
                        tooltip,
                        rule,
                        RuleTooltipMode.DETAIL,
                        "  "
                );
            }
        }
    }

    private static void appendRuleOperations(
            List<Component> tooltip,
            DamageRuleDefinition rule,
            RuleTooltipMode mode,
            String indent
    ) {
        if (rule.operations().isEmpty()) {
            if (mode != RuleTooltipMode.DETAIL) {
                rule.display().description()
                        .filter(RuleTooltipRenderer::hasText)
                        .ifPresent(description ->
                                tooltip.add(
                                        Component.literal(indent)
                                                .append(Component.literal(description))
                                                .withStyle(ChatFormatting.DARK_GREEN)
                                )
                        );
            }

            return;
        }

        for (DamageRuleOperation operation : rule.operations()) {
            tooltip.add(
                    Component.literal(indent)
                            .append(RuleTooltipDescriptions.describeOperation(
                                    operation,
                                    mode
                            ))
                            .withStyle(ChatFormatting.DARK_GREEN)
            );
        }
    }

    private static void appendRuleDisplay(
            List<Component> tooltip,
            DamageRuleDefinition rule,
            String indent,
            boolean includeName
    ) {
        if (includeName) {
            rule.display().name()
                    .filter(RuleTooltipRenderer::hasText)
                    .ifPresent(name ->
                            tooltip.add(
                                    Component.literal(indent)
                                            .append(Component.literal(name))
                                            .withStyle(ChatFormatting.AQUA)
                            )
                    );
        }

        rule.display().description()
                .filter(RuleTooltipRenderer::hasText)
                .ifPresent(description ->
                        tooltip.add(
                                Component.literal(indent)
                                        .append(Component.literal(description))
                                        .withStyle(ChatFormatting.DARK_GRAY)
                        )
                );
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static Map<Identifier, List<DamageRuleDefinition>> groupRules(
            List<DamageRuleDefinition> rules
    ) {
        Map<Identifier, List<DamageRuleDefinition>> groups = new LinkedHashMap<>();

        for (DamageRuleDefinition rule : rules) {
            Identifier groupId = normalizeRuleGroupId(rule.id());

            groups.computeIfAbsent(groupId, ignored -> new ArrayList<>())
                    .add(rule);
        }

        return groups;
    }

    private static Identifier normalizeRuleGroupId(Identifier id) {
        String path = IdentifierText.path(id);

        path = stripSuffix(path, "_base");
        path = stripSuffix(path, "_multi");
        path = stripSuffix(path, "_pre");
        path = stripSuffix(path, "_post");
        path = stripSuffix(path, "_global");
        path = stripSuffix(path, "_final");

        return Identifier.fromNamespaceAndPath(
                IdentifierText.namespace(id),
                path
        );
    }

    private static String stripSuffix(
            String value,
            String suffix
    ) {
        if (value.endsWith(suffix)) {
            return value.substring(0, value.length() - suffix.length());
        }

        return value;
    }

    private static List<DamageRuleCondition> collectConditions(
            List<DamageRuleDefinition> rules
    ) {
        List<DamageRuleCondition> result = new ArrayList<>();

        for (DamageRuleDefinition rule : rules) {
            for (DamageRuleCondition condition : rule.conditions()) {
                if (condition instanceof AlwaysCondition) {
                    continue;
                }

                if (!result.contains(condition)) {
                    result.add(condition);
                }
            }
        }

        return result;
    }

    private static boolean isUnconditionalRule(DamageRuleDefinition rule) {
        if (rule.conditions().isEmpty()) {
            return true;
        }

        for (DamageRuleCondition condition : rule.conditions()) {
            if (!(condition instanceof AlwaysCondition)) {
                return false;
            }
        }

        return true;
    }

    private static MutableComponent formatRuleGroupName(
            Identifier id,
            List<DamageRuleDefinition> rules
    ) {
        for (DamageRuleDefinition rule : rules) {
            if (rule.display().name().isPresent()) {
                String name = rule.display().name().get();

                if (!name.isBlank()) {
                    return Component.literal(name);
                }
            }
        }

        return Component.translatableWithFallback(
                "damage_rule." + IdentifierText.namespace(id) + "." + IdentifierText.path(id),
                humanize(IdentifierText.path(id))
        );
    }

    private static String humanize(String path) {
        String[] parts = path.split("_");
        StringBuilder builder = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }

            if (!builder.isEmpty()) {
                builder.append(' ');
            }

            builder.append(Character.toUpperCase(part.charAt(0)));

            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }

        return builder.toString();
    }
}