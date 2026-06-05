package io.github.naimjeg.damagenexus.api.rule;

public enum DamageRuleProviderType {
    /**
     * Rules supplied by item stacks in an equipment/source slot.
     * <p>
     * The exact slot is described by RuleSourceLocation, not by this provider
     * type. This intentionally replaces the old weapon/armor split.
     */
    ITEM_EQUIPMENT,

    /**
     * Rules supplied by the projectile source reconstructed from the damage
     * source / captured offensive snapshot.
     */
    PROJECTILE_SOURCE,

    /**
     * Rules supplied by an entity-level source.
     * <p>
     * Reserved for future entity attachments/components.
     */
    ENTITY,

    VANILLA_ENCHANTMENT,
    VANILLA_MOB_EFFECT,
    CUSTOM_MOD_EFFECT,

    DAMAGE_TYPE,
    DATAPACK_RULE,

    /**
     * Programmatic rules registered through the public Java API.
     */
    JAVA_API
}

