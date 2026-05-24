package io.github.naimjeg.damagenexus.client.tooltip;

import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleOperation;
import io.github.naimjeg.damagenexus.builtin.rule.condition.AlwaysCondition;
import io.github.naimjeg.damagenexus.builtin.rule.operation.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public final class RuleTooltipRenderer {

    private RuleTooltipRenderer() {}

    public static void renderItemRules(
            List<Component> tooltip,
            List<DamageRuleDefinition> rules,
            boolean detailMode
    ) {
        if (rules.isEmpty()) {
            return;
        }

        for (DamageRuleDefinition rule : rules) {
            if (!rule.display().shouldShowStandalone()) {
                continue;
            }

            appendRuleSummary(
                    tooltip,
                    rule,
                    detailMode
            );
        }
    }

    public static boolean renderDebug(
            List<Component> tooltip,
            List<DamageRuleDefinition> rules,
            boolean sectionAlreadyStarted
    ) {
        if (rules.isEmpty()) {
            return sectionAlreadyStarted;
        }

        if (!sectionAlreadyStarted) {
            tooltip.add(
                    Component.translatable("tooltip.damagenexus.debug.header")
                            .withStyle(ChatFormatting.DARK_AQUA)
            );
        }

        for (DamageRuleDefinition rule : rules) {
            tooltip.add(
                    Component.literal("  ")
                            .append(Component.literal(rule.id().toString()))
                            .withStyle(ChatFormatting.DARK_AQUA)
            );

            tooltip.add(debugLine("source", rule.id().toString()));
            tooltip.add(debugLine("phase", rule.phase().name()));
            tooltip.add(debugLine("mode", "DATA_RULE"));
            tooltip.add(debugLine("role", rule.role().name()));
            tooltip.add(debugLine("priority", Integer.toString(rule.priority())));
            tooltip.add(debugLine("stacking", rule.stacking().name()));

            rule.stackingGroup()
                    .ifPresent(group -> tooltip.add(debugLine(
                            "stacking_group",
                            group.toString()
                    )));

            rule.traceLabel()
                    .ifPresent(trace -> tooltip.add(debugLine(
                            "trace",
                            trace
                    )));

            if (rule.operations().isEmpty()) {
                tooltip.add(debugLine("operation", "<none>"));
                continue;
            }

            for (DamageRuleOperation operation : rule.operations()) {
                appendOperationDebug(tooltip, operation);
            }
        }

        return true;
    }

    private static void appendRuleSummary(
            List<Component> tooltip,
            DamageRuleDefinition rule,
            boolean detailMode
    ) {
        if (detailMode) {
            appendConditionLines(tooltip, rule.conditions());
        }

        if (rule.operations().isEmpty()) {
            rule.display().description()
                    .filter(RuleTooltipRenderer::hasText)
                    .ifPresent(description -> tooltip.add(
                            Component.literal("  ")
                                    .append(Component.literal(description))
                                    .withStyle(ChatFormatting.DARK_GREEN)
                    ));
            return;
        }

        RuleTooltipMode mode = detailMode
                ? RuleTooltipMode.DETAIL
                : RuleTooltipMode.NORMAL;

        for (DamageRuleOperation operation : rule.operations()) {
            tooltip.add(
                    Component.literal("  ")
                            .append(RuleTooltipDescriptions.describeOperation(
                                    operation,
                                    mode
                            ))
                            .withStyle(ChatFormatting.DARK_GREEN)
            );
        }
    }

    private static void appendConditionLines(
            List<Component> tooltip,
            List<DamageRuleCondition> conditions
    ) {
        for (DamageRuleCondition condition : conditions) {
            if (condition instanceof AlwaysCondition) {
                continue;
            }

            tooltip.add(
                    Component.literal("  ")
                            .append(Component.translatable("tooltip.damagenexus.marker.condition"))
                            .append(RuleTooltipDescriptions.describeCondition(
                                    condition,
                                    RuleTooltipMode.DETAIL
                            ))
                            .withStyle(ChatFormatting.GRAY)
            );
        }
    }

    private static void appendOperationDebug(
            List<Component> tooltip,
            DamageRuleOperation operation
    ) {
        tooltip.add(debugLine("operation", operation.type().toString()));

        Optional<Identifier> channel = operationChannel(operation);
        channel.ifPresent(id -> tooltip.add(debugLine("channel", id.toString())));

        operationValue(operation)
                .ifPresent(value -> tooltip.add(debugLine("value", value)));

        operationBucket(operation)
                .ifPresent(bucket -> tooltip.add(debugLine("bucket", bucket)));
    }

    private static Optional<Identifier> operationChannel(
            DamageRuleOperation operation
    ) {
        if (operation instanceof AddBaseDamageOperation addBaseDamage) {
            return Optional.of(addBaseDamage.channelId());
        }

        if (operation instanceof AddTrueDamageOperation addTrueDamage) {
            return Optional.of(addTrueDamage.channelId());
        }

        if (operation instanceof AddChannelPreMultiplierOperation preMultiplier) {
            return Optional.of(preMultiplier.channelId());
        }

        if (operation instanceof AddChannelPostMultiplierOperation postMultiplier) {
            return Optional.of(postMultiplier.channelId());
        }

        if (operation instanceof AddTemporaryResistanceOperation resistance) {
            return Optional.of(resistance.channelId());
        }

        if (operation instanceof AddChannelMitigationOperation mitigation) {
            return Optional.of(mitigation.channelId());
        }

        if (operation instanceof ConvertDamageOperation convertDamage) {
            return Optional.of(convertDamage.fromChannel());
        }

        if (operation instanceof GainExtraDamageOperation gainExtraDamage) {
            return Optional.of(gainExtraDamage.toChannel());
        }

        return Optional.empty();
    }

    private static Optional<String> operationValue(
            DamageRuleOperation operation
    ) {
        if (operation instanceof AddBaseDamageOperation addBaseDamage) {
            return Optional.of(Float.toString(addBaseDamage.value()));
        }

        if (operation instanceof AddTrueDamageOperation addTrueDamage) {
            return Optional.of(Float.toString(addTrueDamage.value()));
        }

        if (operation instanceof AddChannelPreMultiplierOperation preMultiplier) {
            return Optional.of(Float.toString(preMultiplier.value()));
        }

        if (operation instanceof AddChannelPostMultiplierOperation postMultiplier) {
            return Optional.of(Float.toString(postMultiplier.value()));
        }

        if (operation instanceof AddGlobalPreMultiplierOperation globalPre) {
            return Optional.of(Float.toString(globalPre.value()));
        }

        if (operation instanceof AddGlobalPostMultiplierOperation globalPost) {
            return Optional.of(Float.toString(globalPost.value()));
        }

        if (operation instanceof AddTemporaryResistanceOperation resistance) {
            return Optional.of(Float.toString(resistance.value()));
        }

        if (operation instanceof AddChannelMitigationOperation mitigation) {
            return Optional.of(Float.toString(mitigation.value()));
        }

        if (operation instanceof ConvertDamageOperation convertDamage) {
            return Optional.of(Float.toString(convertDamage.ratio()));
        }

        if (operation instanceof GainExtraDamageOperation gainExtraDamage) {
            return Optional.of(Float.toString(gainExtraDamage.ratio()));
        }

        if (operation instanceof OverrideFinalDamageOperation overrideFinalDamage) {
            return Optional.of(Float.toString(overrideFinalDamage.value()));
        }

        return Optional.empty();
    }

    private static Optional<String> operationBucket(
            DamageRuleOperation operation
    ) {
        if (operation instanceof AddBaseDamageOperation addBaseDamage) {
            return Optional.of(addBaseDamage.applicationBucket().name());
        }

        if (operation instanceof AddChannelPreMultiplierOperation preMultiplier) {
            return preMultiplier.preMultiplierBucketId().map(Identifier::toString);
        }

        if (operation instanceof AddGlobalPreMultiplierOperation globalPre) {
            return globalPre.preMultiplierBucketId().map(Identifier::toString);
        }

        return Optional.empty();
    }

    private static Component debugLine(
            String key,
            String value
    ) {
        return Component.literal("    " + key + "=" + value)
                .withStyle(ChatFormatting.DARK_GRAY);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
