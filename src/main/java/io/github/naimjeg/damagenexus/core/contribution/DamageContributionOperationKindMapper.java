package io.github.naimjeg.damagenexus.core.contribution;

import io.github.naimjeg.damagenexus.api.display.DamageContributionOperationKind;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleOperationTypes;
import net.minecraft.resources.Identifier;

public final class DamageContributionOperationKindMapper {

    private DamageContributionOperationKindMapper() {
    }

    public static DamageContributionOperationKind fromRuleOperationType(
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

        return DamageContributionOperationKind.UNKNOWN;
    }
}