package io.github.naimjeg.damagenexus.core.rule;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.display.DamageContributionDescriptor;
import io.github.naimjeg.damagenexus.api.display.DamageContributionOperationKind;
import io.github.naimjeg.damagenexus.api.display.DamageContributionSourceKind;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.*;
import io.github.naimjeg.damagenexus.core.pipeline.DamageMutationResult;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleOperationTypes;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class DamageRuleContributionFactory {

    private DamageRuleContributionFactory() {}

    public static void recordOperation(
            DamageNexusContext ctx,
            DamagePhase runningPhase,
            RuntimeDamageRule runtimeRule,
            DamageRuleOperation operation,
            DamageMutationResult result,
            int operationIndex
    ) {
        if (ctx == null
                || runtimeRule == null
                || operation == null
                || result == null) {
            return;
        }

        ctx.contributions().record(
                result,
                () -> descriptor(
                        runningPhase,
                        runtimeRule,
                        operation,
                        result,
                        operationIndex
                )
        );
    }

    private static DamageContributionDescriptor descriptor(
            DamagePhase runningPhase,
            RuntimeDamageRule runtimeRule,
            DamageRuleOperation operation,
            DamageMutationResult result,
            int operationIndex
    ) {
        DamageRuleDefinition rule = runtimeRule.definition();
        RuleExecutionContext exec = runtimeRule.executionContext();

        Optional<String> displayName =
                rule.display()
                        .name()
                        .map(display -> display.translate()
                                .or(() -> display.text())
                                .or(() -> display.fallback())
                                .orElse(rule.id().toString()));

        return new DamageContributionDescriptor(
                contributionId(rule.id(), operation.type(), operationIndex),
                sourceKind(rule, exec),
                operationKind(operation.type()),
                runningPhase,

                Optional.empty(),
                Optional.empty(),
                Optional.empty(),

                operation.stackingValue(),

                rule.stackingGroup(),
                displayName,
                tooltipLines(rule, exec, operation, result),

                Optional.of(operation.type().toString()),
                true,
                false
        );
    }

    private static DamageContributionSourceKind sourceKind(
            DamageRuleDefinition rule,
            RuleExecutionContext exec
    ) {
        if (rule.display().mode() == RuleDisplayMode.AFFIX_MEMBER) {
            return DamageContributionSourceKind.AFFIX;
        }

        if (exec == null || exec.providerType() == null) {
            return DamageContributionSourceKind.RULE;
        }

        return switch (exec.providerType()) {
            case VANILLA_ENCHANTMENT -> DamageContributionSourceKind.VANILLA_ENCHANTMENT;
            case VANILLA_MOB_EFFECT -> DamageContributionSourceKind.VANILLA_MOB_EFFECT;
            case DAMAGE_TYPE -> DamageContributionSourceKind.VANILLA_DAMAGE_TYPE;
            case JAVA_API -> DamageContributionSourceKind.JAVA_API;
            default -> DamageContributionSourceKind.RULE;
        };
    }

    private static DamageContributionOperationKind operationKind(
            Identifier operationType
    ) {
        if (DamageRuleOperationTypes.ADD_BASE_DAMAGE.equals(operationType)) {
            return DamageContributionOperationKind.ADD_BASE_DAMAGE;
        }

        if (DamageRuleOperationTypes.ADD_TRUE_DAMAGE.equals(operationType)) {
            return DamageContributionOperationKind.ADD_TRUE_DAMAGE;
        }

        if (DamageRuleOperationTypes.ADD_CHANNEL_PRE_MULTIPLIER.equals(operationType)) {
            return DamageContributionOperationKind.ADD_CHANNEL_PRE_MULTIPLIER;
        }

        if (DamageRuleOperationTypes.ADD_GLOBAL_PRE_MULTIPLIER.equals(operationType)) {
            return DamageContributionOperationKind.ADD_GLOBAL_PRE_MULTIPLIER;
        }

        if (DamageRuleOperationTypes.ADD_CHANNEL_POST_MULTIPLIER.equals(operationType)) {
            return DamageContributionOperationKind.ADD_CHANNEL_POST_MULTIPLIER;
        }

        if (DamageRuleOperationTypes.ADD_GLOBAL_POST_MULTIPLIER.equals(operationType)) {
            return DamageContributionOperationKind.ADD_GLOBAL_POST_MULTIPLIER;
        }

        if (DamageRuleOperationTypes.ADD_TEMPORARY_RESISTANCE.equals(operationType)) {
            return DamageContributionOperationKind.ADD_TEMPORARY_RESISTANCE;
        }

        if (DamageRuleOperationTypes.ADD_CHANNEL_MITIGATION.equals(operationType)) {
            return DamageContributionOperationKind.ADD_CHANNEL_MITIGATION;
        }

        if (DamageRuleOperationTypes.ADD_GLOBAL_MITIGATION.equals(operationType)) {
            return DamageContributionOperationKind.ADD_GLOBAL_MITIGATION;
        }

        if (DamageRuleOperationTypes.MULTIPLY_ARMOR_EFFECTIVENESS.equals(operationType)) {
            return DamageContributionOperationKind.MULTIPLY_ARMOR_EFFECTIVENESS;
        }

        if (DamageRuleOperationTypes.CONVERT_DAMAGE.equals(operationType)) {
            return DamageContributionOperationKind.CONVERT_DAMAGE;
        }

        if (DamageRuleOperationTypes.GAIN_EXTRA_DAMAGE.equals(operationType)) {
            return DamageContributionOperationKind.GAIN_EXTRA_DAMAGE;
        }

        if (DamageRuleOperationTypes.OVERRIDE_FINAL_DAMAGE.equals(operationType)) {
            return DamageContributionOperationKind.OVERRIDE_FINAL_DAMAGE;
        }

        if (DamageRuleOperationTypes.CANCEL_DAMAGE.equals(operationType)) {
            return DamageContributionOperationKind.CANCEL_DAMAGE;
        }

        return DamageContributionOperationKind.ADD_BASE_DAMAGE;
    }

    private static List<String> tooltipLines(
            DamageRuleDefinition rule,
            RuleExecutionContext exec,
            DamageRuleOperation operation,
            DamageMutationResult result
    ) {
        if (exec == null) {
            return List.of(
                    "rule=" + rule.id(),
                    "operation=" + operation.type(),
                    "result=" + result
            );
        }

        return List.of(
                "rule=" + rule.id(),
                "operation=" + operation.type(),
                "provider=" + exec.providerType(),
                "source_location=" + exec.sourceLocation(),
                "role=" + exec.role(),
                "result=" + result
        );
    }

    private static Identifier contributionId(
            Identifier ruleId,
            Identifier operationType,
            int operationIndex
    ) {
        String path = "rule/"
                + sanitize(ruleId.getNamespace())
                + "/"
                + sanitize(ruleId.getPath())
                + "/"
                + sanitize(operationType.getPath())
                + "/"
                + Math.max(0, operationIndex);

        return Identifier.fromNamespaceAndPath(DamageNexus.MODID, path);
    }

    private static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }

        return value
                .replace(':', '_')
                .replace('/', '_')
                .replace(' ', '_')
                .toLowerCase(Locale.ROOT);
    }
}