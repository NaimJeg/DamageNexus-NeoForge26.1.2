package io.github.naimjeg.damagenexus.api.rule;

import io.github.naimjeg.damagenexus.builtin.rule.condition.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;

public final class DamageNexusConditions {

    private DamageNexusConditions() {}

    public static DamageRuleCondition always() {
        return new AlwaysCondition();
    }

    public static DamageRuleCondition critical() {
        return new IsCriticalCondition();
    }

    public static DamageRuleCondition targetOnFire() {
        return new TargetOnFireCondition();
    }

    public static DamageRuleCondition damageChannelIs(Identifier channel) {
        return new DamageChannelIsCondition(channel);
    }

    public static DamageRuleCondition attackerHealthBelow(float ratio) {
        return new AttackerHealthBelowCondition(ratio);
    }

    public static DamageRuleCondition attackerHealthAbove(float ratio) {
        return new AttackerHealthAboveCondition(ratio);
    }

    public static DamageRuleCondition targetHealthBelow(float ratio) {
        return new TargetHealthBelowCondition(ratio);
    }

    public static DamageRuleCondition targetHealthAbove(float ratio) {
        return new TargetHealthAboveCondition(ratio);
    }

    public static DamageRuleCondition attackerHasEffect(Identifier effect) {
        return new AttackerHasEffectCondition(effect);
    }

    public static DamageRuleCondition targetHasEffect(Identifier effect) {
        return new TargetHasEffectCondition(effect);
    }

    public static DamageRuleCondition damageTypeIs(Identifier damageType) {
        return new DamageTypeIsCondition(damageType);
    }

    public static DamageRuleCondition damageTypeTag(TagKey<DamageType> tag) {
        return new DamageTypeTagCondition(tag);
    }

    public static DamageRuleCondition targetEntityTypeIs(Identifier entityType) {
        return new TargetEntityTypeIsCondition(entityType);
    }

    public static DamageRuleCondition targetEntityTypeTag(TagKey<EntityType<?>> tag) {
        return new TargetEntityTypeTagCondition(tag);
    }
}