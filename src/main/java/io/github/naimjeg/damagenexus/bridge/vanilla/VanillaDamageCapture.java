package io.github.naimjeg.damagenexus.bridge.vanilla;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.api.DamageNexusTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class VanillaDamageCapture {

    private static final float EPSILON = 0.0001f;

    private static final ThreadLocal<OffensiveEnchantFrame> OFFENSIVE_ENCHANT =
            new ThreadLocal<>();

    private VanillaDamageCapture() {}

    public static void captureModifyDamage(
            ServerLevel level,
            ItemStack weapon,
            Entity victim,
            DamageSource source,
            float inputDamage,
            float outputDamage
    ) {
        OffensiveEnchantFrame previous = OFFENSIVE_ENCHANT.get();

        if (previous != null && ModConfig.isDebugMode()) {
            DamageNexus.LOGGER.warn(
                    "[DN-VanillaCapture] stale offensive enchant frame overwritten. old_attacker={} old_victim={} old_source={} old_weapon={} old_input={} old_output={} new_victim={} new_source={} new_weapon={}",
                    entityName(previous.attacker()),
                    entityName(previous.victim()),
                    sourceId(previous.source()),
                    previous.weapon().getHoverName().getString(),
                    previous.inputDamage(),
                    previous.outputDamage(),
                    entityName(victim),
                    sourceId(source),
                    weapon.getHoverName().getString()
            );
        }

        float delta = outputDamage - inputDamage;

        OFFENSIVE_ENCHANT.set(new OffensiveEnchantFrame(
                source.getEntity(),
                victim,
                source,
                weapon.copy(),
                inputDamage,
                outputDamage,
                delta
        ));

        if (ModConfig.isDebugMode() && Math.abs(delta) > EPSILON) {
            DamageNexus.LOGGER.info(
                    "[DN-VanillaCapture] modifyDamage weapon={} input={} output={} delta={} source={}",
                    weapon.getHoverName().getString(),
                    inputDamage,
                    outputDamage,
                    delta,
                    source.type().msgId()
            );
        }
    }

    public static @Nullable OffensiveSnapshot consumeOffensiveSnapshot(
            DamageSource source,
            LivingEntity victim,
            float eventOriginalDamage
    ) {
        OffensiveEnchantFrame frame = OFFENSIVE_ENCHANT.get();

        if (frame == null) {
            return null;
        }

        try {
            if (frame.source() != source || frame.victim() != victim) {
                return null;
            }

            PreEventDelta preEventDelta = classifyPreEventDelta(
                    source,
                    frame.attacker(),
                    victim,
                    frame.weapon(),
                    frame.outputDamage(),
                    eventOriginalDamage
            );

            return new OffensiveSnapshot(
                    frame.attacker(),
                    frame.victim(),
                    frame.source(),
                    frame.weapon(),
                    frame.inputDamage(),
                    frame.outputDamage(),
                    frame.enchantDelta(),
                    eventOriginalDamage,
                    preEventDelta
            );
        } finally {
            clear();
        }
    }

    public static void clear() {
        OFFENSIVE_ENCHANT.remove();
        MaceDamageCapture.clear();
        SpearDamageCapture.clear();
    }

    private static PreEventDelta classifyPreEventDelta(
            DamageSource source,
            @Nullable Entity attacker,
            LivingEntity victim,
            ItemStack weapon,
            float postEnchantDamage,
            float eventOriginalDamage
    ) {
        float delta = eventOriginalDamage - postEnchantDamage;

        if (Math.abs(delta) <= EPSILON) {
            return PreEventDelta.none(postEnchantDamage, eventOriginalDamage);
        }

        float ratio = Math.abs(postEnchantDamage) > EPSILON
                ? eventOriginalDamage / postEnchantDamage
                : 1.0f;

        String msgId = sourceMsgId(source);
        String sourceId = sourceRegistryId(source);

        SpearDamageCapture.SpearFrame spearFrame =
                attacker != null
                        ? SpearDamageCapture.peekFor(attacker, victim)
                        : null;

        if (spearFrame != null) {
            PreEventDeltaKind kind = switch (spearFrame.mode()) {
                case STAB -> PreEventDeltaKind.SPEAR_STAB_SCALING;
                case CHARGE -> PreEventDeltaKind.SPEAR_CHARGE_SCALING;
            };

            return new PreEventDelta(
                    kind,
                    postEnchantDamage,
                    eventOriginalDamage,
                    delta,
                    ratio,
                    "spear mode=" + spearFrame.mode()
                            + " source=" + sourceId
                            + " base_before_enchant=" + spearFrame.baseDamageBeforeEnchant()
                            + " deals_damage=" + spearFrame.dealsDamage()
                            + " deals_knockback=" + spearFrame.dealsKnockback()
                            + " dismounts=" + spearFrame.dismounts()
            );
        }

        Entity sourceAttacker = source.getEntity();

        if (victim instanceof Player && sourceAttacker instanceof Mob) {
            Difficulty difficulty = victim.level().getDifficulty();

            float expected = switch (difficulty) {
                case PEACEFUL -> 0.0f;
                case EASY -> Math.min(postEnchantDamage, postEnchantDamage * 0.5f + 1.0f);
                case NORMAL -> postEnchantDamage;
                case HARD -> postEnchantDamage * 1.5f;
            };

            if (Math.abs(expected - eventOriginalDamage) <= EPSILON) {
                return new PreEventDelta(
                        PreEventDeltaKind.DIFFICULTY_SCALING,
                        postEnchantDamage,
                        eventOriginalDamage,
                        delta,
                        ratio,
                        "mob_damage_to_player difficulty=" + difficulty.getSerializedName()
                );
            }
        }

        MaceDamageCapture.MaceFrame maceFrame =
                MaceDamageCapture.peekFor(source, victim);

        if (maceFrame != null) {
            return new PreEventDelta(
                    PreEventDeltaKind.SPECIAL_ATTACK_SCALING,
                    postEnchantDamage,
                    eventOriginalDamage,
                    delta,
                    ratio,
                    "mace_smash"
                            + " fall_distance=" + maceFrame.fallDistance()
                            + " returned_bonus=" + maceFrame.returnedBonus()
            );
        }

        if (msgId.contains("mace")
                || msgId.contains("smash")
                || sourceId.contains("mace")
                || sourceId.contains("smash")) {
            return new PreEventDelta(
                    PreEventDeltaKind.SPECIAL_ATTACK_SCALING,
                    postEnchantDamage,
                    eventOriginalDamage,
                    delta,
                    ratio,
                    "special_attack source=" + sourceId
            );
        }

        if (isKineticWeapon(weapon)) {
            if (attacker instanceof Player || "player".equals(msgId)) {
                return new PreEventDelta(
                        PreEventDeltaKind.SPEAR_STAB_SCALING,
                        postEnchantDamage,
                        eventOriginalDamage,
                        delta,
                        ratio,
                        "spear_stab_fallback source=" + sourceId
                );
            }

            if ("mob".equals(msgId)) {
                return new PreEventDelta(
                        PreEventDeltaKind.SPEAR_ATTACK_SCALING,
                        postEnchantDamage,
                        eventOriginalDamage,
                        delta,
                        ratio,
                        "spear_mob_fallback source=" + sourceId
                );
            }

            return new PreEventDelta(
                    PreEventDeltaKind.SPEAR_ATTACK_SCALING,
                    postEnchantDamage,
                    eventOriginalDamage,
                    delta,
                    ratio,
                    "spear_attack_fallback source=" + sourceId
            );
        }

        if (attacker instanceof Player && "player".equals(msgId)) {
            if (ratio <= 1.0f + EPSILON) {
                return new PreEventDelta(
                        PreEventDeltaKind.PLAYER_ATTACK_SCALING,
                        postEnchantDamage,
                        eventOriginalDamage,
                        delta,
                        ratio,
                        "player_attack_scaling source=" + sourceId
                );
            }

            return new PreEventDelta(
                    PreEventDeltaKind.UNKNOWN,
                    postEnchantDamage,
                    eventOriginalDamage,
                    delta,
                    ratio,
                    "player_attack_positive_delta_unclassified source=" + sourceId
            );
        }

        if ("arrow".equals(msgId)
                || "trident".equals(msgId)
                || sourceId.contains("arrow")
                || sourceId.contains("trident")) {
            return new PreEventDelta(
                    PreEventDeltaKind.PROJECTILE_SCALING,
                    postEnchantDamage,
                    eventOriginalDamage,
                    delta,
                    ratio,
                    "projectile_scaling source=" + sourceId
            );
        }

        return new PreEventDelta(
                PreEventDeltaKind.UNKNOWN,
                postEnchantDamage,
                eventOriginalDamage,
                delta,
                ratio,
                "unclassified source=" + sourceId
        );
    }

    private static boolean isKineticWeapon(ItemStack stack) {
        return !stack.isEmpty()
                && stack.has(DataComponents.KINETIC_WEAPON);
    }

    private static String sourceMsgId(DamageSource source) {
        return source.type().msgId();
    }

    private static String sourceRegistryId(DamageSource source) {
        return source.typeHolder()
                .unwrapKey()
                .map(key -> key.identifier().toString())
                .orElse(source.type().msgId());
    }

    public record OffensiveEnchantFrame(
            @Nullable Entity attacker,
            Entity victim,
            DamageSource source,
            ItemStack weapon,
            float inputDamage,
            float outputDamage,
            float enchantDelta
    ) {}

    public record OffensiveSnapshot(
            @Nullable Entity attacker,
            Entity victim,
            DamageSource source,
            ItemStack weapon,
            float preEnchantDamage,
            float postEnchantDamage,
            float enchantDelta,
            float eventOriginalDamage,
            PreEventDelta preEventDelta
    ) {
        public boolean hasEnchantDelta() {
            return Math.abs(enchantDelta) > EPSILON;
        }

        public boolean hasPreEventDelta() {
            return preEventDelta.kind() != PreEventDeltaKind.NONE;
        }
    }

    public record PreEventDelta(
            PreEventDeltaKind kind,
            float postEnchantDamage,
            float eventOriginalDamage,
            float delta,
            float ratio,
            String reason
    ) {
        public static PreEventDelta none(float postEnchantDamage, float eventOriginalDamage) {
            return new PreEventDelta(
                    PreEventDeltaKind.NONE,
                    postEnchantDamage,
                    eventOriginalDamage,
                    0.0f,
                    1.0f,
                    "none"
            );
        }
    }

    private static String entityName(@Nullable Entity entity) {
        return entity != null
                ? entity.getName().getString()
                : "null";
    }

    private static String sourceId(DamageSource source) {
        return source.typeHolder()
                .unwrapKey()
                .map(key -> key.identifier().toString())
                .orElse(source.type().msgId());
    }
}