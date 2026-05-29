package io.github.naimjeg.damagenexus.api.enums;

public enum DamageApplicationBucket {

    /**
     * ATTACK_DAMAGE + Strength / Weakness.
     * Eats vanilla melee cooldown, vanilla melee crit, armor and resistance.
     */
    VANILLA_MELEE_BASE(true, true, false, true),

    /**
     * Sharpness / Smite / Bane delta.
     * Eats vanilla melee cooldown and usually crit according to the current DN table.
     */
    VANILLA_MELEE_ENCHANTMENT(true, true, false, true),

    /**
     * Mace fall bonus, spear speed bonus.
     * Vanilla-specific behavior. By default it remains mitigated.
     */
    VANILLA_WEAPON_SPECIAL(true, true, false, true),

    /**
     * Arrow / trident projectile base damage.
     * Does not eat melee cooldown or melee crit.
     * Projectile crit is handled separately.
     */
    VANILLA_PROJECTILE_BASE(false, false, true, true),

    /**
     * Power / Impaling / projectile bonus.
     * Does not eat melee cooldown or melee crit.
     * Projectile crit is handled separately.
     */
    VANILLA_PROJECTILE_ENCHANTMENT(false, false, true, true),

    /**
     * Normal DN flat damage.
     * Cooldown / crit behavior should be explicitly declared by rules later.
     * It remains armor/resistance mitigated by default.
     */
    DN_RULE_BASE(false, false, false, true),

    /**
     * Real damage / post bonus.
     * Does not eat cooldown, crit, armor, or resistance unless explicitly routed elsewhere.
     */
    DN_TRUE_DAMAGE(false, false, false, false);

    public static final int COUNT = values().length;

    private final boolean affectedByMeleeCooldown;
    private final boolean affectedByMeleeCrit;
    private final boolean affectedByProjectileCrit;
    private final boolean affectedByMitigation;

    DamageApplicationBucket(
            boolean affectedByMeleeCooldown,
            boolean affectedByMeleeCrit,
            boolean affectedByProjectileCrit,
            boolean affectedByMitigation
    ) {
        this.affectedByMeleeCooldown = affectedByMeleeCooldown;
        this.affectedByMeleeCrit = affectedByMeleeCrit;
        this.affectedByProjectileCrit = affectedByProjectileCrit;
        this.affectedByMitigation = affectedByMitigation;
    }

    public boolean affectedByMeleeCooldown() {
        return affectedByMeleeCooldown;
    }

    public boolean affectedByMeleeCrit() {
        return affectedByMeleeCrit;
    }

    public boolean affectedByProjectileCrit() {
        return affectedByProjectileCrit;
    }

    public boolean affectedByMitigation() {
        return affectedByMitigation;
    }
}