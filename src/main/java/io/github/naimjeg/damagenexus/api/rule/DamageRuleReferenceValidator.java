package io.github.naimjeg.damagenexus.api.rule;

import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.core.registry.DamageChannelRegistry;
import io.github.naimjeg.damagenexus.core.registry.PreMultiplierBucketRegistry;
import net.minecraft.resources.Identifier;

public final class DamageRuleReferenceValidator {

    private DamageRuleReferenceValidator() {}

    /**
     * Strict reference validation for datapack-loaded rules.
     *
     * Do not call this from Java API registration during common setup because
     * datapack-defined channels may not be loaded yet.
     */
    public static boolean validateDatapackReferences(
            DamageRuleDefinition rule,
            String source,
            DamageRuleValidator.Policy policy
    ) {
        if (rule == null) {
            return DamageRuleValidator.problem(
                    source,
                    "<null>",
                    "cannot validate references for null rule",
                    policy
            );
        }

        String ruleId = rule.id() == null
                ? "<null>"
                : rule.id().toString();

        boolean valid = true;

        for (DamageRuleCondition condition : rule.conditions()) {
            valid &= validateConditionReference(
                    condition,
                    source,
                    ruleId,
                    policy
            );
        }

        for (DamageRuleOperation operation : rule.operations()) {
            valid &= validateOperationReference(
                    operation,
                    source,
                    ruleId,
                    policy
            );
        }

        return valid;
    }

    private static boolean validateConditionReference(
            DamageRuleCondition condition,
            String source,
            String ruleId,
            DamageRuleValidator.Policy policy
    ) {
        if (condition == null) {
            return true;
        }

        boolean valid = true;

        if (condition instanceof ChannelReferencingCondition channelCondition) {
            for (Identifier channelId : channelCondition.referencedChannels()) {
                valid &= validateChannel(
                        channelId,
                        null,
                        source,
                        ruleId,
                        policy
                );
            }
        }

        return valid;
    }

    private static boolean validateOperationReference(
            DamageRuleOperation operation,
            String source,
            String ruleId,
            DamageRuleValidator.Policy policy
    ) {
        if (operation == null) {
            return true;
        }

        boolean valid = true;

        if (operation instanceof ChannelReferencingOperation channelOperation) {
            for (Identifier channelId : channelOperation.referencedChannels()) {
                valid &= validateChannel(
                        channelId,
                        operation,
                        source,
                        ruleId,
                        policy
                );
            }
        }

        if (operation instanceof PreMultiplierBucketReferencingOperation bucketOperation) {
            for (Identifier bucketId : bucketOperation.referencedPreMultiplierBuckets()) {
                valid &= validatePreMultiplierBucket(
                        bucketId,
                        operation,
                        source,
                        ruleId,
                        policy
                );
            }
        }

        return valid;
    }

    private static boolean validateChannel(
            Identifier channelId,
            DamageRuleOperation operation,
            String source,
            String ruleId,
            DamageRuleValidator.Policy policy
    ) {
        String owner = operation == null
                ? "condition"
                : "operation " + operation.type();

        if (channelId == null) {
            return DamageRuleValidator.problem(
                    source,
                    ruleId,
                    owner + " references null damage channel",
                    policy
            );
        }

        if (DamageChannel.UNTYPED_ID.equals(channelId)) {
            return true;
        }

        if (!DamageChannelRegistry.containsChannel(channelId)) {
            return DamageRuleValidator.problem(
                    source,
                    ruleId,
                    owner + " references unknown damage channel "
                            + channelId
                            + ". This would otherwise fall back to untyped.",
                    policy
            );
        }

        return true;
    }

    private static boolean validatePreMultiplierBucket(
            Identifier bucketId,
            DamageRuleOperation operation,
            String source,
            String ruleId,
            DamageRuleValidator.Policy policy
    ) {
        if (bucketId == null) {
            return DamageRuleValidator.problem(
                    source,
                    ruleId,
                    "operation " + operation.type()
                            + " references null pre-multiplier bucket",
                    policy
            );
        }

        if (!PreMultiplierBucketRegistry.containsPreMultiplierBucket(bucketId)) {
            return DamageRuleValidator.problem(
                    source,
                    ruleId,
                    "operation " + operation.type()
                            + " references unknown pre-multiplier bucket "
                            + bucketId,
                    policy
            );
        }

        return true;
    }
}